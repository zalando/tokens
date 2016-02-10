package org.zalando.stups.tokens;

import java.util.Calendar;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class CalendarTest {

    @Test
    public void testGetMinut() {
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        Assertions.assertThat(minute).isGreaterThan(-1).isLessThan(60);
        System.out.println(minute);
        boolean shouldCheck = minute % 5 == 0;
        System.out.println(shouldCheck);
    }
}
