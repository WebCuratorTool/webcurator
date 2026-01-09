package org.webcurator.rest;

import org.junit.Test;

import java.util.TimeZone;

public class TargetDTOTest {

    @Test
    public void testTimeZone(){
        TimeZone defaultZone=TimeZone.getDefault();
        System.out.println(defaultZone);
        System.out.println("ID="+defaultZone.getID());
        System.out.println("displayName="+defaultZone.getDisplayName());
    }
}
