package io.tongchain.justsdk.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StringUtils {

    public String strToUpperCase(String str){
        str = str.replace(str.substring(0, 1), str.substring(0, 1).toUpperCase());
        return str;
    }

    /**
     * 判断输入是否为空，包含null，""," "等情况
     * @param text 输入字符
     * @return 如果输入为空，那么返回true，否则返回false
     */
    public static final boolean isNull(String text) {
        if (text == null || "".equals(text) || "null".equals(text) || "".equals(text.trim())) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * 判断输入是否为空，包含null，""," "等情况
     * @param text 输入字符
     * @return 如果输入为空，那么返回true，否则返回false
     */
    public static final boolean isNull(Object text) {
        return isNull(text + "");
    }
    /**
     * 判断输入是否不为空，包含null，""," "等情况
     * @param text 输入字符
     * @return 如果输入为空，那么返回false，否则返回true
     */
    public static final boolean isNotNull(String text) {
        return !isNull(text);
    }

    /**
     * 判断输入是否不为空，包含null，""," "等情况
     * @param text 输入字符
     * @return 如果输入为空，那么返回false，否则返回true
     */
    public static final boolean isNotNull(Object text) {
        return isNotNull(text + "");
    }
    /**
     * 如果text为空（包含null，""," "等情况），那么返回默认值
     * @param text 输入字符
     * @param defaultText 当text为空时，返回的默认值
     * @return 如果text为空（包含null，""," "等情况），那么返回默认值，否则返回text
     */
    public static final String notNull(String text, String defaultText) {
        return isNull(text) ? defaultText : text;
    }

    public static String nullToVarchar(String str) {
        if (str == null) {
            return "";
        }
        return str;
    }
    public static final String toHex(byte hash[]) {
        StringBuffer buf = new StringBuffer(hash.length * 2);
        String stmp = "";
        for (int i = 0; i < hash.length; i++) {
            stmp = (Integer.toHexString(hash[i] & 0XFF));
            if (stmp.length() == 1)
                buf.append(0).append(stmp);
            else
                buf.append(stmp);
        }
        return buf.toString();
    }

    public static final byte[] hexToBytes(String hex) {
        if (null == hex)
            return new byte[0];
        int len = hex.length();
        byte[] bytes = new byte[len / 2];
        String stmp = null;
        try {
            for (int i = 0; i < bytes.length; i++) {
                stmp = hex.substring(i * 2, i * 2 + 2);
                bytes[i] = (byte) Integer.parseInt(stmp, 16);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
        return bytes;
    }

    public static final String replace(String line, String oldString,String newString) {
        int i = 0;
        if ((i = line.indexOf(oldString, i)) >= 0) {
            char[] line2 = line.toCharArray();
            char[] newString2 = newString.toCharArray();
            int oLength = oldString.length();
            StringBuffer buf = new StringBuffer(line2.length);
            buf.append(line2, 0, i).append(newString2);
            i += oLength;
            int j = i;
            while ((i = line.indexOf(oldString, i)) > 0) {
                buf.append(line2, j, i - j).append(newString2);
                i += oLength;
                j = i;
            }
            buf.append(line2, j, line2.length - j);
            return buf.toString();
        }
        return line;
    }

    public static final String escapeHTMLTags(String input) {
        if (input == null || input.length() == 0) {
            return input;
        }
        StringBuffer buf = new StringBuffer();
        char ch = ' ';
        for (int i = 0; i < input.length(); i++) {
            ch = input.charAt(i);
            switch (ch) {
                case '<':
                    buf.append("&lt;");
                    break;
                case '>':
                    buf.append("&gt;");
                    break;
                case '&':
                    buf.append("&amp;");
                    break;
                case '"':
                    buf.append("&quot;");
                    break;
                default:
                    buf.append(ch);
            }
        }
        return buf.toString();
    }

    public static final String escapeJS(String input) {
        if (input == null || input.length() == 0) {
            return input;
        }
        StringBuffer buf = new StringBuffer();
        char ch = ' ';
        for (int i = 0; i < input.length(); i++) {
            ch = input.charAt(i);
            switch (ch) {
                case '<':
                    buf.append("&lt;");
                    break;
                case '>':
                    buf.append("&gt;");
                    break;
                case '&':
                    buf.append("&amp;");
                    break;
                case '"':
                    buf.append("&quot;");
                    break;
                case '\\':
                    buf.append("\\\\");
                    break;
                case '\r':
                    buf.append("\\r");
                    break;
                case '\n':
                    buf.append("\\n");
                    break;
                default:
                    buf.append(ch);
            }
        }
        return buf.toString();
    }

    public static final String escapeJsHtml(String input) {
        if (input == null || input.length() == 0) {
            return input;
        }
        StringBuffer buf = new StringBuffer();
        char ch = ' ';
        for (int i = 0; i < input.length(); i++) {
            ch = input.charAt(i);
            switch (ch) {
                case '<':
                    buf.append("&lt;");
                    break;
                case '>':
                    buf.append("&gt;");
                    break;
                case '&':
                    buf.append("&amp;");
                    break;
                case '"':
                    buf.append("&quot;");
                    break;
                case '\\':
                    buf.append("\\\\");
                    break;
                case '\r':
                    buf.append("\\r");
                    break;
                case '\n':
                    buf.append("\\n");
                    break;
                case '\'':
                    buf.append("\\'");
                    break;
                default:
                    buf.append(ch);
            }
        }
        return buf.toString();
    }



    public static final String escapeForXML(String string) {
        if (string == null || string.length() == 0) {
            return string;
        }
        char[] sArray = string.toCharArray();
        StringBuffer buf = new StringBuffer(sArray.length);
        char ch;
        for (int i = 0; i < sArray.length; i++) {
            ch = sArray[i];
            if (ch == '<') {
                buf.append("&lt;");
            } else if (ch == '>') {
                buf.append("&gt;");
            } else if (ch == '"') {
                buf.append("&quot;");
            } else if (ch == '&') {
                buf.append("&amp;");
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }

    public static final String unescapeFromXML(String string) {
        string = replace(string, "&lt;", "<");
        string = replace(string, "&gt;", ">");
        string = replace(string, "&quot;", "\"");
        return replace(string, "&amp;", "&");
    }

    public static final String arrToString(String[] str) {
        String x = "";
        for (int i = 0; i < str.length; i++) {
            x = x + str[i];
            if (i < str.length - 1) {
                x = x + ",";
            }
        }
        return x;
    }

    public static final String listToString(List<String> list) {
        StringBuffer str = new StringBuffer("");
        for (Iterator<String> iter = list.iterator(); iter.hasNext();) {
            str.append(iter.next());
            if (iter.hasNext()) {
                str.append(",");
            }
        }
        return str.toString();
    }

    public static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0;) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isString(String str) {
        for (int i = str.length(); --i >= 0;) {
            if (!Character.isLetter(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String formatTitle(int len, String value) throws Exception {
        if (value == null) {
            return "";
        }
        if (value.getBytes("GBK").length > len && len - 3 > 0) {
            return value.substring(0, len - 3) + "...";
        }
        return value;
    }



    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否乱码
     * @param strName
     * @return
     */
    public static boolean isMessyCode(String strName) {
        Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
        Matcher m = p.matcher(strName);
        String after = m.replaceAll("");
        String temp = after.replaceAll("\\p{P}", "");
        char[] ch = temp.trim().toCharArray();
//		float chLength = ch.length;
//		float count = 0;
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (!Character.isLetterOrDigit(c)) {
                if (!isChinese(c)) {
//					count = count + 1;
                    return true;
                }
            }
        }
//		float result = count / chLength;
//		if (result > 0.4) {
//			return true;
//		} else {
//			return false;
//		}
        return false;
    }

    /**
     * 进行字符串替换
     * @param source 要进行替换操作的字符串
     * @param from   要替换的字符串
     * @param to     要替换成的字符串
     * @return 替换后的字符串
     */
    static public String replaceAll(String source, String from, String to)
    {
        if ( (source == null) || source.equals("") || (from == null) ||
                (to == null) || from.equals("") || from.equals(to)) {
            return source;
        }

        StringBuffer sb = new StringBuffer(source.length());
        String s = source;
        int index = s.indexOf(from);
        int fromLen = from.length();

        while (index != -1) {
            sb.append(s.substring(0, index));
            sb.append(to);
            s = s.substring(index + fromLen);
            index = s.indexOf(from);
        }

        return sb.append(s).toString();
    }

    /**
     * 对字符串做UTF8编码
     */
    static public String utf8Enc(String src)
    {
        String result = "";
        try {
            result = URLEncoder.encode(src, "utf-8");
        }
        catch (Exception ex) {
        }
        return result;
    }

    public static int parseInt(String str, int defaultValue){
        if(str==null){
            return defaultValue;
        }
        try{
            return Integer.parseInt(str);
        }
        catch(NumberFormatException e){
            return defaultValue;
        }
    }

    public static double parseDouble(String str, double defaultValue){
        if(str==null){
            return defaultValue;
        }
        try{
            return Double.parseDouble(str);
        }
        catch(NumberFormatException e){
            return defaultValue;
        }
    }

    /**
     * 返回独立的String列表
     */
    public static String[] getDistinctString(String[] res)
    {
        HashSet<String> hs = new HashSet<String>();
        ArrayList<String> results = new ArrayList<String>();

        for (int i=0; i < res.length; i++)
        {
            String str = res[i];
            if (!hs.contains(str)) {
                hs.add(str);
                results.add(str);
            }
        }

        return (String[]) results.toArray(new String[0]);
    }

    /**
     * 获取百分数格式数据
     */
    public static String getDecimalFormat(float value, int digitNum)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("0.");
        for (int i=0; i < digitNum; i++) {
            sb.append("0");
        }

        DecimalFormat df = new DecimalFormat(sb.toString());
        return df.format(value*100) + "%";
    }

    /**
     * 获取百分数格式数据
     */
    public static String getDecimalFormat(String value, int digitNum)
    {
        Float f = 0.0f;

        try {
            f = Float.parseFloat(value);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return getDecimalFormat(f, digitNum);
    }
    /***
     * isEqual:比较关键词是否相同吗？相同的话返回true不相同返回false
     * 参数：keywords从textarea获取的值
     * 参数：fileKeywords 原来文件中存的值
     * ******/
    public static boolean isEqual(String keywords,String fileKeywords){
        keywords=isNull(keywords)? "" : keywords;
        fileKeywords=isNull(fileKeywords)? "" : fileKeywords;
        String kws =keywords.trim().replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "").replaceAll("((\r\n)|\n)", ",");
        String fkws=fileKeywords.trim().replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "").replaceAll("((\r\n)|\n)", ",");
        String[] kwsplit = kws.split(",");
        String[] fkwsplit = fkws.split(",");
        Arrays.sort(kwsplit);
        Arrays.sort(fkwsplit);
        if(kwsplit.length == fkwsplit.length)
        {
            boolean falg=true;
            for(int i=0; i<kwsplit.length; i++){
                if(!fkwsplit[i].trim().equals(kwsplit[i].trim())){
                    falg=false;
                    break;
                }
            }
            return falg;
        }else{
            return false;
        }
    }
    /************
     * 去掉textarea值的空行（/r/n|/n）
     *  return:string
     * ******/
    public static String deleteCRLFOnce(String value) {
        if (isNotNull(value)) {
            return value.replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "").trim();
        } else {
            return "";
        }
    }


    /******
     *
     * 去除字符串中的空格、回车、换行符、制表符
     *
     * ***/
    public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    public static String decode(String data) {
        try {
            data = data.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
            data = data.replaceAll("\\+", "%2B");
            data = URLDecoder.decode(data, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }


}
