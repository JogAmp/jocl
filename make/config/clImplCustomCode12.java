
    /** If null, OpenCL is not available on this machine. */
    static final DynamicLibraryBundle dynamicLookupHelper;
    protected static final CLProcAddressTable12 addressTable;

    static {
        addressTable = new CLProcAddressTable12();
        dynamicLookupHelper = AccessController.doPrivileged(new PrivilegedAction<DynamicLibraryBundle>() {
                                  public DynamicLibraryBundle run() {
                                      final DynamicLibraryBundle bundle = new DynamicLibraryBundle(new CLDynamicLibraryBundleInfo());
                                      if(!bundle.isToolLibLoaded()) {
                                          // couldn't load native CL library
                                          // TODO: log this?
                                          return null;
                                      }
                                      if(!bundle.isLibComplete()) {
                                          System.err.println("Couln't load native CL/JNI glue library");
                                          return null;
                                      }
                                      addressTable.reset(bundle);
                                      /** Not required nor forced
                                      if( !initializeImpl() ) {
                                          System.err.println("Native initialization failure of CL/JNI glue library");
                                          return null;
                                      } */
                                      return bundle;
                                  } } );
    }

    /**
     * Accessor.
     * @returns true if OpenCL is available on this machine.
     */
    public static boolean isAvailable() { return dynamicLookupHelper != null; }
    public static CLProcAddressTable12 getCLProcAddressTable() { return addressTable; }

    static long clGetExtensionFunctionAddress(long clGetExtensionFunctionAddressHandle, java.lang.String procname)
    {
        if (clGetExtensionFunctionAddressHandle == 0) {
            throw new RuntimeException("Passed null pointer for method \"clGetExtensionFunctionAddress\"");
        }
        return dispatch_clGetExtensionFunctionAddressStatic(procname, clGetExtensionFunctionAddressHandle);
    }

    public CLAbstractImpl12() {
    }

    /** Entry point (through function pointer) to C language function: <br> <code> void* clGetExtensionFunctionAddress(const char *  fname); </code>    */
    long clGetExtensionFunctionAddress(String fname)  {

        final long __addr_ = addressTable._addressof_clGetExtensionFunctionAddress;
        if (__addr_ == 0) {
            throw new UnsupportedOperationException("Method \"clGetExtensionFunctionAddress\" not available");
        }
        return dispatch_clGetExtensionFunctionAddressStatic(fname, __addr_);
    }

    /** Entry point (through function pointer) to C language function: <br> <code> void* clGetExtensionFunctionAddress(const char *  fname); </code>    */
    private static native long dispatch_clGetExtensionFunctionAddressStatic(String fname, long procAddress);

