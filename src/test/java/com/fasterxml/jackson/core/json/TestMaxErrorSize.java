package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.*;

/**
 * Test size of parser error messages
 */
public class TestMaxErrorSize
    extends com.fasterxml.jackson.core.BaseTest
{
    private final static int EXPECTED_MAX_TOKEN_LEN = 256; // ParserBase.MAX_ERROR_TOKEN_LENGTH
    
    private final JsonFactory JSON_F = new JsonFactory();

    public void testLongErrorMessage() throws Exception
    {
        _testLongErrorMessage(MODE_INPUT_STREAM);
        _testLongErrorMessage(MODE_INPUT_STREAM_THROTTLED);
    }

    public void testLongErrorMessageReader() throws Exception
    {
        _testLongErrorMessage(MODE_READER);
    }

    private void _testLongErrorMessage(int mode) throws Exception
    {
        final String DOC = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        		+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        		+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        		+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        assertTrue(DOC.length() > 256);
        JsonParser jp = createParser(JSON_F, mode, DOC);
        try {
            jp.nextToken();
            fail("Expected an exception for unrecognized token");
        } catch (JsonParseException jpe) {
        	String msg = jpe.getMessage();
          final String expectedPrefix = "Unrecognized token '";
          final String expectedSuffix = "...': was expecting";
        	verifyException(jpe,  expectedPrefix);
          verifyException(jpe,  expectedSuffix);
        	assertTrue(msg.contains(expectedSuffix));
          int tokenLen = msg.indexOf (expectedSuffix) - expectedPrefix.length();
        	assertEquals(EXPECTED_MAX_TOKEN_LEN, tokenLen);
        }
        jp.close();
    }

    public void testShortErrorMessage() throws Exception
    {
        _testShortErrorMessage(MODE_INPUT_STREAM);
        _testShortErrorMessage(MODE_INPUT_STREAM_THROTTLED);
        _testShortErrorMessage(MODE_READER);
    }

    public void _testShortErrorMessage(int mode) throws Exception
    {
        final String DOC = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        assertTrue(DOC.length() < 256);
        JsonParser jp = createParser(JSON_F, mode, DOC);
        try {
            jp.nextToken();
            fail("Expected an exception for unrecognized token");
        } catch (JsonParseException jpe) {
            String msg = jpe.getMessage();
            final String expectedPrefix = "Unrecognized token '";
            final String expectedSuffix = "': was expecting";
            verifyException(jpe,  expectedPrefix);
            verifyException(jpe,  expectedSuffix);
            int tokenLen = msg.indexOf(expectedSuffix) - expectedPrefix.length();
            assertEquals(DOC.length(), tokenLen);
        }
        jp.close();
    }

    // [core#1698]: every parser backend must honor max error token length;
    //   `UTF8DataInputJsonParser` used to accumulate the whole token instead
    public void testErrorTokenLengthBoundedInAllModes() throws Exception
    {
        final int maxLen = EXPECTED_MAX_TOKEN_LEN;
        // Broken token far longer than the limit: must be truncated, not accumulated in full
        final String doc = _buildBrokenJsonOfLength(50 * maxLen);
        // limit, plus appended "..."
        final String expToken = _brokenToken(maxLen) + "...";

        for (int mode : ALL_MODES) {
            try (JsonParser p = createParser(JSON_F, mode, doc)) {
                p.nextToken();
                p.nextToken();
                fail("Should not pass, mode: "+mode);
            } catch (JsonProcessingException e) {
                assertEquals("mode: "+mode, expToken, _unrecognizedToken(e.getMessage()));
            }
        }
    }

    // Short broken token (below limit) must be reported verbatim, without
    // duplicated leading char, in all modes
    public void testShortErrorTokenReportedExactlyInAllModes() throws Exception
    {
        final String doc = "{\"key\":abc!}";

        for (int mode : ALL_MODES) {
            try (JsonParser p = createParser(JSON_F, mode, doc)) {
                p.nextToken();
                p.nextToken();
                fail("Should not pass, mode: "+mode);
            } catch (JsonProcessingException e) {
                assertEquals("mode: "+mode, "abc", _unrecognizedToken(e.getMessage()));
            }
        }
    }

    // [core#1698]: huge broken token via DataInput must not be accumulated
    //   into the (unbounded) error message
    public void testHugeErrorTokenBoundedDataInput() throws Exception
    {
        final String doc = _buildBrokenJsonOfLength(1000000);
        try (JsonParser p = createParser(JSON_F, MODE_DATA_INPUT, doc)) {
            p.nextToken();
            p.nextToken();
            fail("Should not pass");
        } catch (JsonProcessingException e) {
            final String msg = e.getMessage();
            assertTrue("Error message too long: "+msg.length(), msg.length() < 1000);
            assertEquals(_brokenToken(EXPECTED_MAX_TOKEN_LEN) + "...", _unrecognizedToken(msg));
        }
    }

    // Extracts X from "Unrecognized token 'X': was expecting ..."
    private String _unrecognizedToken(String msg)
    {
        final String prefix = "Unrecognized token '";
        final int start = msg.indexOf(prefix);
        assertTrue("message: "+msg, start >= 0);
        final int end = msg.indexOf("': was expecting", start);
        assertTrue("message: "+msg, end > start);
        return msg.substring(start + prefix.length(), end);
    }

    private String _buildBrokenJsonOfLength(int len)
    {
        return "{\"key\":" + _brokenToken(len) + "!}";
    }

    // Varied (not repeating single) chars so that duplicated/dropped chars are detectable
    private String _brokenToken(int len)
    {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append((char) ('a' + (i % 26)));
        }
        return sb.toString();
    }
}

