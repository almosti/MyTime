package com.almosti.mytime;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.action.ViewActions;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BasicFunctionTest {
    MainActivity mainActivity;

    @Before
    public void SetUp(){
        Intent intent = new Intent();
        intent.setClassName("com.almosti.mytime", MainActivity.class.getName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainActivity = (MainActivity)getInstrumentation().startActivitySync(intent);
    }

    @Test
    public void AddNewTime(){
        String titleString="标题"+Math.random();
        titleString=titleString.substring(0,10);
        String markerString="备注"+Math.random();
        int timeDistance=(int)(Math.random()*10);
        int cycle = (int) (Math.random() * 30);
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.edit_title)).perform(replaceText(titleString));
        onView(withId(R.id.edit_remark)).perform(replaceText(markerString));
        onView(withId(R.id.edit_time_section)).perform(longClick());
        onView(withId(R.id.dialog_edit_future_time)).perform(replaceText(String.valueOf(timeDistance)));
        onView(withId(R.id.dialog_edit_future_time_select)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.edit_repeat_section)).perform(click());
        onView(withText("自定义")).perform(click());
        onView(withId(R.id.edit_dialog_edit)).perform(replaceText(String.valueOf(cycle)));
        onView(withText("确定")).perform(click());
        //onView(withId(R.id.edit_image_section)).perform(click());
        //onView(withText("Pictures")).perform(click());
        onView(withId(R.id.edit_save)).perform(click());
    }

    @Test
    public void AddNewTimeByData(){
        TimePage page;

    }

}