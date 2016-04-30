/*
 * Copyright 2010 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */

package com.jogamp.opencl;

import com.jogamp.opencl.test.util.UITestCase;
import com.jogamp.opencl.util.CLBuildConfiguration;
import com.jogamp.opencl.util.CLProgramConfiguration;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.CLProgram.Status;
import com.jogamp.opencl.util.CLBuildListener;
import com.jogamp.opencl.llb.CL;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;
import static java.lang.System.*;
import static com.jogamp.common.os.Platform.is32Bit;
import static com.jogamp.opencl.CLException.newException;
import static com.jogamp.opencl.CLProgram.CompilerOptions.*;
import static com.jogamp.opencl.llb.CL12.CL_KERNEL_GLOBAL_WORK_SIZE;
import static com.jogamp.opencl.llb.CL.CL_SUCCESS;

/**
 *
 * @author Michael Bien, et.al
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CLProgramTest extends UITestCase {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();


    @Test
    public void test01Enums() {

        // CLProgram enums
        for (final Status e : Status.values()) {
            assertEquals(e, Status.valueOf(e.STATUS));
        }
    }

    @Test
    public void test02RebuildProgram() throws IOException {
        final CLContext context = CLContext.create();
        final CLProgram program = context.createProgram(getClass().getResourceAsStream("testkernels.cl"));

        // only test kernel creation error on unbuilt program if we're not on AMD -- as of
        // 3/8/2014, AMD drivers segfault on this instead of returning CL_INVALID_PROGRAM_EXECUTABLE
        if(!context.getPlatform().isVendorAMD()) {
            try{
                program.createCLKernels();
                fail("expected exception but got none :(");
            }catch(final CLException ex) {
                out.println("got expected exception:  "+ex.getCLErrorString());
                assertEquals(ex.errorcode, CL.CL_INVALID_PROGRAM_EXECUTABLE);
            }
        }

        out.println(program.getBuildStatus());
        program.build();
        out.println(program.getBuildStatus());

        assertTrue(program.isExecutable());

        CLKernel kernel = program.createCLKernel("VectorAddGM");
        assertNotNull(kernel);

        // rebuild
        // 1. release kernels (internally)
        // 2. build program
        program.build();
        assertTrue(program.isExecutable());
        out.println(program.getBuildStatus());

        // try again with rebuilt program
        kernel = program.createCLKernel("VectorAddGM");
        assertNotNull(kernel);

        context.release();
    }

    @Test
    public void test03ProgramBinaries() throws IOException {
        final CLContext context = CLContext.create();
        CLProgram program = context.createProgram(getClass().getResourceAsStream("testkernels.cl"))
                                   .build(ENABLE_MAD, WARNINGS_ARE_ERRORS);

        // obtain binaries
        final Map<CLDevice, byte[]> binaries = program.getBinaries();
        assertFalse(binaries.isEmpty());

        final CLDevice[] devices = program.getCLDevices();
        for (final CLDevice device : devices) {
            assertTrue(binaries.containsKey(device));
        }

        // 1. release program
        // 2. re-create program with old binaries
        program.release();

        assertFalse(program.isExecutable());

        assertNotNull(program.getBinaries());
        assertEquals(program.getBinaries().size(), 0);

        assertNotNull(program.getBuildLog());
        assertEquals(program.getBuildLog().length(), 0);

        assertNotNull(program.getSource());
        assertEquals(program.getSource().length(), 0);

        assertNotNull(program.getCLDevices());
        assertEquals(program.getCLDevices().length, 0);

        // make sure kernel creation does nothing after program release 
        {
            final Map<String, CLKernel> kernels = program.createCLKernels();
            assertNotNull(kernels);
            assertEquals(kernels.size(), 0);
        }
        assertNull(program.createCLKernel("foo"));

        program = context.createProgram(binaries);

        // as of 10/25/2015, Intel shows recreated programs as executable 
        if(!context.getPlatform().isVendorIntel())
        	assertFalse(program.isExecutable());
        else
        	assertTrue(program.isExecutable());
        	
        assertNotNull(program.getCLDevices());
        assertTrue(program.getCLDevices().length != 0);

        assertNotNull(program.getBinaries());
        // as of 10/25/2015, Intel shows recreated programs binaries as having size 
        if(!context.getPlatform().isVendorIntel())
        	assertEquals(program.getBinaries().size(), 0);
        else
        	assertTrue(program.getBinaries().size() > 0);

        assertNotNull(program.getBuildLog());
        assertTrue(program.getBuildLog().length() != 0);

        assertNotNull(program.getSource());
       	assertEquals(program.getSource().length(), 0);

        // only test kernel creation error on unbuilt program if we're not on AMD or Intel -- as of
        // (3/8/2014, 10/31/2015) (AMD, Intel) drivers segfault on this instead of returning CL_INVALID_PROGRAM_EXECUTABLE
        if(!context.getPlatform().isVendorAMD() && !context.getPlatform().isVendorIntel()) {
            try{
                final Map<String, CLKernel> kernels = program.createCLKernels();
                fail("expected an exception from createCLKernels but got: "+kernels);
            }catch(final CLException ex) {
                // expected, not built yet
            }
        }

        out.println(program.getBuildStatus());
        program.build();
        out.println(program.getBuildStatus());

        assertNotNull(program.createCLKernel("Test"));

        assertTrue(program.isExecutable());

        context.release();

    }

    private void builderImpl(final boolean sync) throws IOException, ClassNotFoundException, InterruptedException {
        final CLContext context = CLContext.create();
        CLProgram program = context.createProgram(getClass().getResourceAsStream("testkernels.cl"));

        // same as program.build()
        program.prepare().build();

        assertTrue(program.isExecutable());


        // complex build
        program.prepare().withOption(ENABLE_MAD)
                         .forDevice(context.getMaxFlopsDevice())
                         .withDefine("RADIUS", 5)
                         .withDefine("ENABLE_FOOBAR")
                         .build();

        assertTrue(program.isExecutable());

        // reusable builder
        final CLBuildConfiguration builder = CLProgramBuilder.createConfiguration()
                                     .withOption(ENABLE_MAD)
                                     .forDevices(context.getDevices())
                                     .withDefine("RADIUS", 5)
                                     .withDefine("ENABLE_FOOBAR");

        out.println(builder);

        if( sync ) {
            // sync build test
            final CLProgram outerProgram = program;

            builder.setProgram(program).build();
            assertEquals(outerProgram, program);
        } else {
            // async build test
            final CountDownLatch countdown = new CountDownLatch(1);
            final CLProgram outerProgram = program;

            final CLBuildListener buildCallback = new CLBuildListener() {
                @Override
                public void buildFinished(final CLProgram program) {
                    assertEquals(outerProgram, program);
                    countdown.countDown();
                }
            };
            builder.setProgram(program).build(buildCallback);
            countdown.await();
        }

        assertTrue(program.isExecutable());

        // serialization test
        final File file = tmpFolder.newFile("foobar.builder");
        final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
        builder.save(oos);
        oos.close();

        // build configuration
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        final CLBuildConfiguration buildConfig = CLProgramBuilder.loadConfiguration(ois);
        ois.close();

        assertEquals(builder, buildConfig);

        buildConfig.build(program);
        assertTrue(program.isExecutable());

        // program configuration
        ois = new ObjectInputStream(new FileInputStream(file));
        final CLProgramConfiguration programConfig = CLProgramBuilder.loadConfiguration(ois, context);
        assertNotNull(programConfig.getProgram());
        ois.close();
        program = programConfig.build();
        assertTrue(program.isExecutable());

        // cloneing
        assertEquals(builder, builder.clone());

        context.release();
    }


    @Test
    public void test10BuilderSync() throws IOException, ClassNotFoundException, InterruptedException {
        builderImpl(true);
    }

    @Test
    public void test11BuilderAsync() throws IOException, ClassNotFoundException, InterruptedException {
        builderImpl(false);
    }

    private static final String test20KernelSource = "__attribute__((reqd_work_group_size(1, 1, 1))) kernel void foo(float a, int b, short c) { }\n";

    @Test
    public void test20Kernel() {
        final CLContext context = CLContext.create();

        try{
            final CLProgram program = context.createProgram(test20KernelSource).build();
            assertTrue(program.isExecutable());

            final CLKernel kernel = program.createCLKernel("foo");
            assertNotNull(kernel);

            final long[] wgs = kernel.getCompileWorkGroupSize(context.getDevices()[0]);

            out.println("compile workgroup size: " + wgs[0]+" "+wgs[1]+" "+wgs[2]);

            assertEquals(1, wgs[0]);
            assertEquals(1, wgs[1]);
            assertEquals(1, wgs[2]);

            // put args test
            assertEquals(0, kernel.position());

            kernel.putArg(1.0f);
            assertEquals(1, kernel.position());

            kernel.putArg(2);
            assertEquals(2, kernel.position());

            kernel.putArg((short)3);
            assertEquals(3, kernel.position());

            try{
                kernel.putArg(3);
                fail("exception not thrown");
            }catch (final IndexOutOfBoundsException expected){ }

            assertEquals(3, kernel.position());
            assertEquals(0, kernel.rewind().position());

        }finally{
            context.release();
        }

    }

    @Test
    public void test21AllKernels() {
        final String source = "kernel void foo(int a) { }\n"+
                        "kernel void bar(float b) { }\n";

        final CLContext context = CLContext.create();
        try{
            final CLProgram program = context.createProgram(source).build();
            assertTrue(program.isExecutable());

            final Map<String, CLKernel> kernels = program.createCLKernels();
            for (final CLKernel kernel : kernels.values()) {
                out.println("kernel: "+kernel.toString());
            }

            assertNotNull(kernels.get("foo"));
            assertNotNull(kernels.get("bar"));

            kernels.get("foo").setArg(0, 42);
            kernels.get("bar").setArg(0, 3.14f);


        }finally{
            context.release();
        }

    }

    /**
     * Test of getting new kernel work group information, including those from OpenCL versions newer than 1.1.
     */
    @Test
    public void test22KerneWorkGrouplInfo() {
        final CLContext context = CLContext.create();

        try{
            final CLProgram program = context.createProgram(test20KernelSource).build();
            assertTrue(program.isExecutable());

            final CLKernel kernel = program.createCLKernel("foo");
            assertNotNull(kernel);

            final long pwgsm = kernel.getPreferredWorkGroupSizeMultiple(context.getDevices()[0]);
            out.println("preferred workgroup size multiple: " + pwgsm);

            final long pms = kernel.getPrivateMemSize(context.getDevices()[0]);
            out.println("private mem size: " + pms);
        }finally{
            context.release();
        }
    }

//    @Test
    public void test60Load() throws IOException, ClassNotFoundException, InterruptedException {
        for(int i = 0; i < 100; i++) {
            test02RebuildProgram();
            test11BuilderAsync();
            test03ProgramBinaries();
        }
    }

    public static void main(final String[] args) throws IOException {
        final String tstname = CLProgramTest.class.getName();
        org.junit.runner.JUnitCore.main(tstname);
    }

}
