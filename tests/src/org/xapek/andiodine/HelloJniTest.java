package org.xapek.andiodine;

import android.test.ActivityInstrumentationTestCase2;

import org.xapek.andiodine.IodineMain;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class org.xapek.andiodine.HelloJniTest \
 * com.example.HelloJni.tests/android.test.InstrumentationTestRunner
 */
public class HelloJniTest extends ActivityInstrumentationTestCase2<org.xapek.andiodine.IodineMain> {
    public HelloJniTest() {
        super(IodineMain.class);
    }

}
