/**
 * Copyright (c) C24 Technologies Limited. All rights reserved.
 */
package biz.c24.io.mule;

import biz.c24.io.api.ParserException;
import biz.c24.io.api.Utils;
import biz.c24.io.api.data.Base64BinaryDataType;
import biz.c24.io.api.data.BooleanDataType;
import biz.c24.io.api.data.ByteDataType;
import biz.c24.io.api.data.DecimalDataType;
import biz.c24.io.api.data.DoubleDataType;
import biz.c24.io.api.data.FloatDataType;
import biz.c24.io.api.data.GenericDateDataType;
import biz.c24.io.api.data.ISO8601DateDataType;
import biz.c24.io.api.data.ISO8601DateTimeDataType;
import biz.c24.io.api.data.ISO8601TimeDataType;
import biz.c24.io.api.data.IntDataType;
import biz.c24.io.api.data.IntegerDataType;
import biz.c24.io.api.data.LongDataType;
import biz.c24.io.api.data.NegativeIntegerDataType;
import biz.c24.io.api.data.NonNegativeIntegerDataType;
import biz.c24.io.api.data.NonPositiveIntegerDataType;
import biz.c24.io.api.data.NumberDataType;
import biz.c24.io.api.data.PositiveIntegerDataType;
import biz.c24.io.api.data.SQLBlob;
import biz.c24.io.api.data.SQLBlobDataType;
import biz.c24.io.api.data.SQLClob;
import biz.c24.io.api.data.SQLClobDataType;
import biz.c24.io.api.data.SQLDateDataType;
import biz.c24.io.api.data.SQLTimeDataType;
import biz.c24.io.api.data.SQLTimestampDataType;
import biz.c24.io.api.data.ShortDataType;
import biz.c24.io.api.data.SimpleDataType;
import biz.c24.io.api.data.StringDataType;
import biz.c24.io.api.data.UnsignedByteDataType;
import biz.c24.io.api.data.UnsignedIntDataType;
import biz.c24.io.api.data.UnsignedLongDataType;
import biz.c24.io.api.data.UnsignedShortDataType;
import biz.c24.io.api.data.WhiteSpaceEnum;

class C24Util {
    
    private C24Util() {
    }
    
    /**
     * TODO - This is cloned from LoaderUtils.parseObjectAsXML. Refactor!
     * @param t
     * @param str
     * @return
     * @throws ParserException
     */
    @SuppressWarnings("all") 
    static Object parseObject(SimpleDataType t, String str) throws ParserException {
        if (t instanceof NumberDataType) {
            if (str.startsWith("+")) {
                str = str.substring(1);
            }
            if (str.startsWith(".")) {
                str = "0" + str;
            }
        }
        if (!(t instanceof StringDataType)) {
            str = Utils.whitespace(str, WhiteSpaceEnum.COLLAPSE);
        }
        if (t instanceof GenericDateDataType) {
            GenericDateDataType date = (GenericDateDataType)t;
            if (date.isDate() && date.isTime()) {
                return ((ISO8601DateTimeDataType)ISO8601DateTimeDataType.getInstance()).parseDate(str);
            }
            if (date.isDate()) {
                return ((ISO8601DateDataType)ISO8601DateDataType.getInstance()).parseDate(str);
            }
            if (date.isTime()) {
                return ((ISO8601TimeDataType)ISO8601TimeDataType.getInstance()).parseDate(str);
            }
        }
        if (t instanceof SQLDateDataType) {
            return new java.sql.Date(
                    ((ISO8601DateDataType)ISO8601DateDataType.getInstance()).parseDate(str).getTime()
            );
        }
        if (t instanceof SQLTimeDataType) {
            return new java.sql.Time(((ISO8601TimeDataType)ISO8601TimeDataType.getInstance()).parseDate(str).getTime());
        }
        if (t instanceof SQLTimestampDataType) {
            return new java.sql.Timestamp(
                    ((ISO8601DateTimeDataType)ISO8601DateTimeDataType.getInstance()).parseDate(str).getTime()
            );
        }
        if (t instanceof SQLBlobDataType) {
            return new SQLBlob((byte[])((Base64BinaryDataType)Base64BinaryDataType.getInstance()).parseObject(str));
        }
        if (t instanceof SQLClobDataType) {
            return new SQLClob(str);
        }
        if (t instanceof ByteDataType && ((ByteDataType)t).isFormatUsed()) {
            return ((SimpleDataType)ByteDataType.getInstance()).parseObject(str);
        }
        if (t instanceof ShortDataType && ((ShortDataType)t).isFormatUsed()) {
            return ((SimpleDataType)ShortDataType.getInstance()).parseObject(str);
        }
        if (t instanceof IntDataType && ((IntDataType)t).isFormatUsed()) {
            return ((SimpleDataType)IntDataType.getInstance()).parseObject(str);
        }
        if (t instanceof LongDataType && ((LongDataType)t).isFormatUsed()) {
            return ((SimpleDataType)LongDataType.getInstance()).parseObject(str);
        }
        if (t instanceof UnsignedByteDataType && ((UnsignedByteDataType)t).isFormatUsed()) {
            return ((SimpleDataType)UnsignedByteDataType.getInstance()).parseObject(str);
        }
        if (t instanceof UnsignedShortDataType && ((UnsignedShortDataType)t).isFormatUsed()) {
            return ((SimpleDataType)UnsignedShortDataType.getInstance()).parseObject(str);
        }
        if (t instanceof UnsignedIntDataType && ((UnsignedIntDataType)t).isFormatUsed()) {
            return ((SimpleDataType)UnsignedIntDataType.getInstance()).parseObject(str);
        }
        if (t instanceof UnsignedLongDataType && ((UnsignedLongDataType)t).isFormatUsed()) {
            return ((SimpleDataType)UnsignedLongDataType.getInstance()).parseObject(str);
        }
        if (t instanceof PositiveIntegerDataType && ((PositiveIntegerDataType)t).isFormatUsed()) {
            return ((SimpleDataType)PositiveIntegerDataType.getInstance()).parseObject(str);
        }
        if (t instanceof NonNegativeIntegerDataType && ((NonNegativeIntegerDataType)t).isFormatUsed()) {
            return ((SimpleDataType)NonNegativeIntegerDataType.getInstance()).parseObject(str);
        }
        if (t instanceof NegativeIntegerDataType && ((NegativeIntegerDataType)t).isFormatUsed()) {
            return ((SimpleDataType)NegativeIntegerDataType.getInstance()).parseObject(str);
        }
        if (t instanceof NonPositiveIntegerDataType && ((NonPositiveIntegerDataType)t).isFormatUsed()) {
            return ((SimpleDataType)NonPositiveIntegerDataType.getInstance()).parseObject(str);
        }
        if (t instanceof IntegerDataType && ((IntegerDataType)t).isFormatUsed()) {
            return ((SimpleDataType)IntegerDataType.getInstance()).parseObject(str);
        }
        if (t instanceof DecimalDataType && ((DecimalDataType)t).isFormatUsed()) {
            return ((SimpleDataType)DecimalDataType.getInstance()).parseObject(str);
        }
        if (t instanceof FloatDataType && ((FloatDataType)t).isFormatUsed()) {
            return ((SimpleDataType)FloatDataType.getInstance()).parseObject(str);
        }
        if (t instanceof DoubleDataType && ((DoubleDataType)t).isFormatUsed()) {
            return ((SimpleDataType)DoubleDataType.getInstance()).parseObject(str);
        }
        if (t instanceof BooleanDataType) {
            if (str.equals("1")) {
                return Boolean.TRUE;
            }
            if (str.equals("0")) {
                return Boolean.FALSE;
            }
        }

        return t.parseObject(str);
    }

    

}
