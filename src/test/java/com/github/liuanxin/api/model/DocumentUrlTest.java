package com.github.liuanxin.api.model;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class DocumentUrlTest {

    @Test
    public void testEmptyString() {
        String returnJson = "";
        boolean commentInReturnExample = true;
        boolean commentInReturnExampleWithLevel = false;
        List<DocumentReturn> returnList = new ArrayList<>();
        String result = DocumentUrl.commentJson(returnJson, commentInReturnExample, commentInReturnExampleWithLevel, returnList);
        assertEquals("", result);
    }

    @Test
    public void testNullString() {
        String returnJson = null;
        boolean commentInReturnExample = true;
        boolean commentInReturnExampleWithLevel = false;
        List<DocumentReturn> returnList = new ArrayList<>();
        String result = DocumentUrl.commentJson(returnJson, commentInReturnExample, commentInReturnExampleWithLevel, returnList);
        assertEquals("", result);
    }

    @Test
    public void testAddCommentToJSON() {
        String returnJson = "{\"key\": \"value\", \"name\": \"John\"}";
        boolean commentInReturnExample = true;
        boolean commentInReturnExampleWithLevel = false;
        List<DocumentReturn> returnList = new ArrayList<>();
        returnList.add(new DocumentReturn("key", "The value of the key", "discription"));
        returnList.add(new DocumentReturn("name", "The name of the person", "discription"));
        String result = DocumentUrl.commentJson(returnJson, commentInReturnExample, commentInReturnExampleWithLevel, returnList);
        String expectedResult = "{\n"
                + "  \"key\" : \"value\",  /* discription */\n"
                + "  \"name\" : \"John\"  /* discription */\n"
                + "}";
        assertEquals(expectedResult, result);
    }


}

