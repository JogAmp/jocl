/*
 * Copyright 2009 - 2010 JogAmp Community. All rights reserved.
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

package com.jogamp.opencl.gl;

import com.jogamp.opencl.llb.CL;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLException;
import com.jogamp.opencl.CLImageFormat;
import com.jogamp.opencl.llb.impl.CLImageFormatImpl;

import java.nio.Buffer;

/**
 * 2D OpenCL image representing an 2D OpenGL texture.
 * @author Michael Bien, et.al.
 */
public class CLGLTexture2d<B extends Buffer> extends CLGLImage2d<B> implements CLGLTexture {

    public final int target;

    public final int mipMapLevel;

    public CLGLTexture2d(final CLContext context, final B directBuffer, final CLImageFormat format, final CLImageInfoAccessor accessor, final int target, final int mipLevel, final int width, final int height, final long id, final int glid, final int flags) {
        super(context, directBuffer, format, accessor, width, height, id, glid, flags);
        this.target = target;
        this.mipMapLevel = mipLevel;
    }

    static <B extends Buffer> CLGLTexture2d<B> createFromGLTexture2d(final CLContext context, final B directBuffer, final int target, final int texture, final int mipLevel, final int flags) {

        CLGLBuffer.checkBuffer(directBuffer, flags);

        final CL cl = getCL(context);
        final int[] result = new int[1];

        final long id = cl.clCreateFromGLTexture2D(context.ID, flags, target, mipLevel, texture, result, 0);
        CLException.checkForError(result[0], "can not create CLGLTexture2d from texture #"+texture+".");

        final CLImageInfoAccessor accessor = new CLImageInfoAccessor(cl, id);

        final CLImageFormat format = createUninitializedImageFormat();
        accessor.getInfo(CL.CL_IMAGE_FORMAT, CLImageFormatImpl.size(), format.getFormatImpl().getBuffer(), null);

        final int width = (int)accessor.getLong(CL.CL_IMAGE_WIDTH);
        final int height = (int)accessor.getLong(CL.CL_IMAGE_HEIGHT);

        return new CLGLTexture2d<B>(context, directBuffer, format, accessor, target, mipLevel, width, height, id, texture, flags);

    }

    @Override
    public int getTextureTarget() {
        return target;
    }

    @Override
    public int getMipMapLevel() {
        return mipMapLevel;
    }

    @Override
    public GLObjectType getGLObjectType() {
        return GLObjectType.GL_OBJECT_TEXTURE2D;
    }


}
