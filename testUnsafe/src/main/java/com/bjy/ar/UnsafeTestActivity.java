package com.bjy.ar;

import android.os.Bundle;
import android.util.Log;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import net.bramp.unsafe.UnsafeHelper;

import java.lang.reflect.Field;

public class UnsafeTestActivity extends UnityPlayerActivity {

    public String str = "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcUFhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCAQ4B4ADASIAAhEBAxEB/8QAHAAAAgMBAQEBAAAAAAAAAAAAAAECAwQFBgcI/8QATRAAAQQBAwMDAgQFAwMCAgEVAQACAxEhBBIxBUFREyJhBnEygZGhBxQjQrFSwdEzYuEV8CRD8RZTcpIXJTQ1Y4LSREVUVWRzk5TT\n" +
            "wv/EABoBAQEBAQEBAQAAAAAAAAAAAAABAgMEBQb/xAAtEQEBAQEAAgICAgIBAwQDAQAAARECAxIE\n" +
            "IRMxQVEFImEUMoFxobHBQpHR8P/aAAwDAQACEQMRAD8A9WjuV+UI/qf6jdVdd6sQf/5yT/8ASVzf\n" +
            "qH6kLC49c6rV1/8Ahkn/ACoj9TpHg0aXzz/7rvQR+LR9UA//AKUf/wCms7v4zfTwJH8n1XHiKP8A\n" +
            "/TQfSu+UqyV81P8AGf6e/wD3Lq3/APaj/wD9iX/3Z/p7NaLq2f8A8lH/AP7EH0qqwjsuP/8AVX9P\n" +
            "f/x7pP8A/mR/8o/+qr6er/8AH3Sf/wDMj/5Qdc9wg8LkN+qOgO/D1zpR+2rj/wCVMfUfQzx1rpp+\n" +
            "Bqmf8qDphLhSSpFATCSYQMKQURzSmFUOqQmBSYGEHP1jz27Lmy6lzDuvjgLrayHcCc0uNPCbN8KK\n" +
            "16Xqjtw3Ouz3Xag1wcabycrx0jCwEs5Vmj1ckFOlweA0j91z6ivdN1OLJCT9USSBVLzP/rDGbQ6Q\n" +
            "bgePC1afXhxGQbWYrqvJcsGs0jpQRnK1Q6ljvutjJGOB4W8R47WdLijY4uYy6NE2vjP8StC71PXZ\n" +
            "/Znb+y/QHVoxIXAYpeG650Zmpa/cwW/J+6zYPzvfxXx4V+jmbBqWSu/sNr2P1F9IvY98unAaSbJP\n" +
            "GF4zVaWbTPcyZhBGOFZZUx7qCc6iCKW8Xf7LbE8UW3QIzlec+nZjJ08i69N20hduN+aWVcvXtEcx\n" +
            "bYyLWJxOPhdrqQMkY9wIHArhcJxyRfKsFUji2x5KpIO457qTiCSQbUcrcZxEjCm+Wohtd769pHZQ\n" +
            "Iu1X3paHc0Ovfq2gStG4c7RQCvcC5xa2s9+QF5ouLHW0kOW2Dqjw4NeBsHccppjuRNb6e0gkHij3\n" +
            "Wn/09k5aZmlwAweK+Fm0Oqhcd7n1xtBFWu/o4Q5l0fjN3+amq4sv0/piymNDfkErFH9Muc122Q0D\n" +
            "to5v5C9j6YLLIG3m1awNZTi5oBHOMBFcHpn0QNVII/VMgsVii3819f8ApD6K6R0KEPGmY6cgXIbs\n" +
            "8/K8Z0/6s6V03e31PUm4HavlWa3+Iukhic/1y5wHDRwudalfWZdVpdLCRG5oPgBeZ6718RxSRwua\n" +
            "X8HIK8no+t6jW9Obq3EgyDc0HxeFwOt9Q/k4jK4miRuKxIa74c97jI9wcXZJpQ1o/obu4x9ws3T5\n" +
            "/wD4BriLxYN3YVjC6be0i2nhb1HI0sgEY7fHK3C9oI5WefT/AMu+gdpBtXRvDhjxax9mG4n3XV1+\n" +
            "qqkJBLQRwLFK4qotOT2XXn9JiEbAXbgCCPJVhzYASqhlNp5C2hcKQ8JJhRUgK/IJnHN32QPlHdAj\n" +
            "nlK8E/sgfKOyxYQnfss+qm2Nc21dI9rR7nUFxZpHSykmq7LlWoV2TygnHPfugZGRwfKsiYZZg1o5\n" +
            "7LDTV03Su1EtkbmUMgcL1WnhEcQaOFR0rRiKIWKXTDQBZ4VlFbRtytAcdhws73bnn/TilpjNx1WV\n" +
            "vnplidIXklxFjHFLJqdKd+/NrqwaapCSQQrNZGDHigT8LpO4mOHpoRI/a4d16Tp/TA5uWhcGCmag\n" +
            "X5X0HoDGzwMx7uybpjl6vpLJIHhzAbGF8q+r+gS9Olk1emiJjA3SAHt5H6r9Du0W9v4b7VS8v9Qd\n" +
            "GbKxwLOQmD8/xaouNuq/NUFr/mGOoCx+S2/Vf05N06V0+maTET7m9x8rzsc1Y3fdEdhrrUgwn3NG\n" +
            "K5WXTSb+XAA9qytTaAJPZVGTUH+05pZW7Q73AkeAaWnWOY11g38rEL9ziBg4Cg+tfwkZXT4Mg57f\n" +
            "dffvp9pEIXxL+F0BbooCQAavwvufRG1pxYpRXYYMKYUWqYUwNMBAQmLh0oSe2Nx7qxZtc/Zpnn4V\n" +
            "xXzn62npkp7C1+ftdMZtXM5pO0uOD+i+1fX2o26eXF4PB7r4i1hNk8uJJVRUwE2aoHlp7Kxtgkdl\n" +
            "ICgldH48qoiwAgEK1vJ8KHN7TkYUgfb8hEw3GhjlUPdg+VY80LWaRworQzvtpOL8qpxzf+VbJVWD\n" +
            "+SzvOCggarwkT/wh2cqBODaDHquCVyJiWuNDHhdnUN9uTjyuXPm6yFitRHS35Ab9locSe6yQOIcB\n" +
            "VZWo/Cxiok9ldp3nPcqg5UozRwrE10ozhWOcaptLPC/caV5G0/IW4ihw+OFnmJv4WzlVyxBwwMrS\n" +
            "MXY91fpgAdxdXYi1WWEHOFOMAIrQ8nBBCrDwMOKhI7NDurdLpciSa7HA8oL4Wmt3ZTA7kgIsCgMI\n" +
            "wAsoR8IaKJKeSMJ4DSO9rQg7J+37pN+EyT2Sbygl/cDjCkc1nHKQoBHCoK7qLscqeKtUarcYztdR\n" +
            "80s0U6nUCMGqcsbNRZBcQADlRc17QbwAsshPJWVjU/WhjSWk/cFUeoZX2TZPKzBpe9boNM4fiFFI\n" +
            "psaTikph6THkZPFLaxjYxb7oLn6t295DRZWojnyHcSSKVvTtK7VatkTQTZtx+E3taAP9Rx+a959E\n" +
            "9DcyEanUNokZJFYUI26FkXSOnHcGtI91DGV43qvUJeozl8l7e2f/AH8LsfV3Ud2oGn01bRhxHdec\n" +
            "iifIa2kHsEkXVIBB+PKbGufhoLvsOFsZoybLzTRytHrwaRoawNc4jK1IzqrT9MkefeaaBnstYm0e\n" +
            "islrZJmigR/x4XL1GulkxvIHhYXSUPdZVG7X9Y1ErPTY4Nj8AC1yHOLjZQ51lJVDTAQMqyNtlANa\n" +
            "VLbQKuDRXwjagzcoCuLcFQ2HsLQDXKW6j8FQr8k6ws1dWBysa7Cz3SA4i6XOmtAcpNOTZWYP+VJr\n" +
            "lldaG8AeBSbRigqg84tdPpWi/mpCZAdgxnuoi7pOl9UkuBLa5Bpd4PJYOMClNmlZHGGN4q8ClVLG\n" +
            "WgkFBb6tAXx5Uf5sZFlYJXPNtKgHHtz8qjqslD220p2ucwkVnK0xTFzdrhkcqi45SOfySJxjm0ZH\n" +
            "2KLBwkCb4x4QSADaiTQRQXc4SQDzXBCQCijuhjfcRYom0HF2tGijJfdjAxeUHR0jQ2LBDnc2OPsq\n" +
            "dTfqZIJr7K0ktbTq3KnJFV3wUiKxYbt7E2UNaGn5U6wUEXdcjP3XSIXY+ExxgZSAq8KYbR8fC0hN\n" +
            "bk/JVu2rrPwk0WaH6qdLlY1KW21ID2kXyKUhfCm0Y4r7rOLoaKbSta01jKQCsDmty40PPhDUmtDW\n" +
            "EucGtGSSvNdc65t/o6d4LTe6jkKP1D1klxg0zga5K8rPKWt3E32/NQKeVxc50n4nHcb8rHLJk/Kc\n" +
            "0pdZWcW44ykim3c91dyupodKA25GuIPNGlDp+mPIHxa7GmhaBt4+Sp1dDj3Ee7kYVxeI4yLF+FKN\n" +
            "ha2sX2CWnim1c/pRe0NIJNXysql07Ty6vU78CNv4hS7wZvIZE07eBXZPT6b+XjbAwWeDWLXZ6boa\n" +
            "c1oG5zufhBb0bp1lrAOckhfQei9PbBHYFErJ0HpbY2AlelYwNaAgGtoKbRhACsjZaAYOy1wMUYmL\n" +
            "bp4kGjSswOaXTgaqdNHwt8TaQSjFFXAeEmjCkEDCEDupJgQTQhAISpNAIQhAdkBCEwCEITAIQhAh\n" +
            "wmhRQCEJIGhAQgEfCOUKKEd0JhEASUlFFCRTtJAIQhAUhMpIEE0Jo";

    public static UnsafeTestActivity instance = getInstance();  //单例类

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        //初始化控件id
        initView();

    }

    public static UnsafeTestActivity getInstance() {
        if (instance == null) {
            instance = new UnsafeTestActivity();
        }

        return instance;
    }

    public long addressString;

    public void initView() {
//        Log.d("str====", UnsafeHelper.toAddress(str) + "");
//        Log.d("str====2", UnsafeHelper.fromAddress(UnsafeHelper.toAddress(str)) + "");
//        UnityPlayer.UnitySendMessage("Canvas", "FromAndroidMsg", addressString);

        UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("runOnUiThread",""+addressString);
                    addressString = UnsafeHelper.toAddress(str);
                    Log.d("runOnUiThread2222",""+addressString);

                } catch (Exception e) {

                }
            }
        });
    }

    public long returnAddress() {
        Log.d("returnAddress", "addressString = " + addressString);
        return addressString;
    }


    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static Unsafe reflectGetUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Exception e) {
            Log.e(e.getMessage(), e + "");
            return null;
        }
    }

    public enum Unsafe {

    }


}