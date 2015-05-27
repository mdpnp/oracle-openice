package org.mdpnp.hiberdds.testapp;

import java.io.PrintStream;

import com.rti.dds.infrastructure.BadKind;
import com.rti.dds.infrastructure.Bounds;
import com.rti.dds.typecode.TCKind;
import com.rti.dds.typecode.TypeCode;

public class SchemaGen {
    public static String oracletype(TypeCode typeCode, boolean length) throws BadKind {
        TCKind kind = typeCode.kind();
        
        if(TCKind.TK_ALIAS.equals(kind)) {
            return oracletype(typeCode.content_type(), length);
        } else if(TCKind.TK_STRING.equals(kind)) {
            return "VARCHAR2" + (length ? ("("+typeCode.length()+")"):"");
        } else if(TCKind.TK_WSTRING.equals(kind)) {
            return "NVARCHAR2" + (length ? ("("+typeCode.length()+")"):"");
        } else if(TCKind.TK_LONG.equals(kind)) {
            return "NUMBER" + (length ? "(10)":"");
        } else if(TCKind.TK_FLOAT.equals(kind)) {
            return "FLOAT";
        } else if(TCKind.TK_ENUM.equals(kind)) {
            return "VARCHAR2"+(length?"(32)":"");
        } else if(TCKind.TK_STRUCT.equals(kind)) {
            if("ice::Time_t".equals(typeCode.name())) {
                return "TIMESTAMP"+(length?"(6)":"");
            } else {
                return null;
            }
        } else {
            System.err.println("Unknown " + kind);
            return null;
        }

    }
    
    
    public static void printCreateTables(PrintStream out, TypeCode typeCode, String sqlName) throws BadKind, Bounds {
        
        // Instance Table
        out.print("CREATE TABLE ");
        out.print(sqlName);
        out.println(" (");
        out.print("    ");
        out.print(sqlName);
        out.println("_ID NUMBER(10) NOT NULL,");
        
        for(int i = 0; i < typeCode.member_count(); i++) {
            if(typeCode.is_member_key(i)) {
                String typeDef = oracletype(typeCode.member_type(i), true);
                if(null != typeDef) {
                   out.print("    ");
                   out.print(typeCode.member_name(i).toUpperCase());
                   out.print(" ");
                   out.print(typeDef);
                   out.println(",");
                }
            }
        }
        
        out.print("    CONSTRAINT PK_");
        out.print(sqlName);
        out.print(" PRIMARY KEY (");
        out.print(sqlName);
        out.println("_ID),");
        out.print("    CONSTRAINT ");
        out.print(sqlName);
        out.print("_UNIQUE_INDEX UNIQUE (");

        boolean first = true;
        for(int i = 0; i < typeCode.member_count(); i++) {
            if(typeCode.is_member_key(i) && null != oracletype(typeCode.member_type(i), true)) {
                if(!first) {
                    out.print(",");
                } else {
                    first = false;
                }
                out.print(typeCode.member_name(i).toUpperCase());
            }
        }
        out.println(")");
        out.println(");");
        
        // a sequence for the instances
        out.print("CREATE SEQUENCE ");
        out.print(sqlName);
        out.println("_SEQ MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE;");
        
        // lifecycle table
        out.print("CREATE TABLE ");
        out.print(sqlName);
        out.println("_LIFECYCLE (");
        out.print("    ");
        out.print(sqlName);
        out.println("_LIFECYCLE_ID NUMBER(10) NOT NULL,");
        out.println("    ALIVE CHAR(1),");
        out.println("    TIME TIMESTAMP(6),");
        out.print("    ");
        out.print(sqlName);
        out.println("_ID NUMBER(10) NOT NULL,");
        out.print("    CONSTRAINT PK_");
        out.print(sqlName);
        out.print("_LIFECYCLE PRIMARY KEY (");
        out.print(sqlName);
        out.println("_LIFECYCLE_ID),");
        out.print("    CONSTRAINT FK_");
        out.print(sqlName);
        out.print("_LIFECYCLE FOREIGN KEY (");
        out.print(sqlName);
        out.println("_ID)");
        out.print("        REFERENCES ");
        out.print(sqlName);
        out.print("(");
        out.print(sqlName);
        out.println("_ID)");
        out.println("        ON DELETE CASCADE");
        out.println(");");
        
        // Index on the lifecycle table
//        out.print("CREATE INDEX FK_LIFECYCLE_");
//        out.print(sqlName);
//        out.print(" ON ");
//        out.print(sqlName);
//        out.print("_LIFECYCLE (");
//        out.print(sqlName);
//        out.println("_ID);");
        
        // lifecycle integrity constraint
//        out.print("ALTER TABLE ");
//        out.print(sqlName);
//        out.println("_LIFECYCLE");
//        out.print("    ADD FOREIGN KEY (");
//        out.print(sqlName);
//        out.println("_ID)");
//        out.print("    REFERENCES ");
//        out.print(sqlName);
//        out.print(" (");
//        out.print(sqlName);
//        out.println("_ID)");
//        out.println("    ON DELETE CASCADE;");
        
        // a sequence for the lifecycles
        out.print("CREATE SEQUENCE ");
        out.print(sqlName);
        out.println("_LIFECYCLE_SEQ MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE;");
        
        // Sample Table
        out.print("CREATE TABLE ");
        out.print(sqlName);
        out.println("_SAMPLE (");
        out.print("    ");
        out.print(sqlName);
        out.println("_SAMPLE_ID NUMBER(10) NOT NULL,");
        out.print("    ");
        out.print(sqlName);
        out.println("_ID NUMBER(10) NOT NULL,");
        out.println("    SOURCE_TIME TIMESTAMP(6) NOT NULL,");
        
        for(int i = 0; i < typeCode.member_count(); i++) {
            if(!typeCode.is_member_key(i)) {
                String typeDef = oracletype(typeCode.member_type(i), true);
                if(null != typeDef) {
                   out.print("    ");
                   out.print(typeCode.member_name(i).toUpperCase());
                   out.print(" ");
                   out.print(typeDef);
                   out.println(",");
                }
            }
        }
        
        out.print("    CONSTRAINT PK_");
        out.print(sqlName);
        out.print("_SAMPLE PRIMARY KEY (");
        out.print(sqlName);
        out.println("_SAMPLE_ID),");
        out.print("    CONSTRAINT FK_");
        out.print(sqlName);
        out.print("_SAMPLE FOREIGN KEY (");
        out.print(sqlName);
        out.println("_ID)");
        out.print("        REFERENCES ");
        out.print(sqlName);
        out.print("(");
        out.print(sqlName);
        out.println("_ID)");
        out.println("        ON DELETE CASCADE");

        out.println(") ORGANIZATION INDEX;");

        // foreign key constraint for sample table
//        out.print("CREATE INDEX FK_");
//        out.print(sqlName);
//        out.print("_ID ON ");
//        out.print(sqlName);
//        out.print("_SAMPLE (");
//        out.print(sqlName);
//        out.println("_ID);");
        
        // sample integrity constraint
//        out.print("ALTER TABLE ");
//        out.print(sqlName);
//        out.println("_SAMPLE");
//        out.print("    ADD FOREIGN KEY (");
//        out.print(sqlName);
//        out.println("_ID)");
//        out.print("    REFERENCES ");
//        out.print(sqlName);
//        out.print(" (");
//        out.print(sqlName);
//        out.println("_ID)");
//        out.println("    ON DELETE CASCADE;");
        
        // a sequence for the samples
        out.print("CREATE SEQUENCE ");
        out.print(sqlName);
        out.println("_SAMPLE_SEQ MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE;");

        // upsert samples
        out.print("CREATE OR REPLACE FUNCTION UPSERT_");
        out.print(sqlName);
        out.println("(");
        first = true;
        for(int i = 0; i < typeCode.member_count(); i++) {
            if(typeCode.is_member_key(i)) {
                String typeDef = oracletype(typeCode.member_type(i), false);
                if(null != typeDef) {
                   if(!first) {
                       out.print(", ");
                   } else {
                       out.print("    ");
                       first = false;
                   }
                   out.print(typeCode.member_name(i).toUpperCase());
                   out.print(" IN ");
                   out.print(typeDef);
                }
            }
        }
        out.println(")");
        out.println("RETURN NUMBER");
        out.println("AS");
        out.print(sqlName);
        out.println("_ID NUMBER(10);");
        out.println("BEGIN");
        out.print("    SELECT ");
        out.print(sqlName);
        out.print(".");
        out.print(sqlName);
        out.print("_ID INTO UPSERT_");
        out.print(sqlName);
        out.print(".");
        out.print(sqlName);
        out.print("_ID FROM ");
        out.println(sqlName);
        out.println("    WHERE");
        first = true;
        for(int i = 0; i < typeCode.member_count(); i++) {
            if(typeCode.is_member_key(i)) {
                String typeDef = oracletype(typeCode.member_type(i), false);
                if(null != typeDef) {
                   out.print("        ");
                   if(!first) {
                       out.print("AND ");
                   } else {
                       first = false;
                   }
                   out.print("((");
                   out.print(sqlName);
                   out.print(".");
                   out.print(typeCode.member_name(i).toUpperCase());
                   out.print(" IS NULL AND UPSERT_");
                   out.print(sqlName);
                   out.print(".");
                   out.print(typeCode.member_name(i).toUpperCase());
                   out.print(" IS NULL) OR ");
                   out.print(sqlName);
                   out.print(".");
                   out.print(typeCode.member_name(i).toUpperCase());
                   out.print(" = UPSERT_");
                   out.print(sqlName);
                   out.print(".");
                   out.print(typeCode.member_name(i).toUpperCase());
                   out.println(") ");
                }
            }
        }
        out.println(";");
        out.print("RETURN UPSERT_");
        out.print(sqlName);
        out.print(".");
        out.print(sqlName);
        out.println("_ID;");
        out.println("EXCEPTION WHEN NO_DATA_FOUND THEN");
        out.println("BEGIN");
        out.print("    SELECT ");
        out.print(sqlName);
        out.print("_SEQ.nextval INTO UPSERT_");
        out.print(sqlName);
        out.print(".");
        out.print(sqlName);
        out.println("_ID FROM DUAL;");
        out.print("    INSERT INTO ");
        out.print(sqlName);
        out.print(" (");
        out.print(sqlName);
        out.print("_ID");
        for(int i = 0; i < typeCode.member_count(); i++) {
            if(typeCode.is_member_key(i)) {
                String typeDef = oracletype(typeCode.member_type(i), false);
                if(null != typeDef) {
                   out.print(", ");
                   out.print(typeCode.member_name(i).toUpperCase());
                }
            }
        }
        out.print(") VALUES (UPSERT_");
        out.print(sqlName);
        out.print(".");
        out.print(sqlName);
        out.print("_ID");
        for(int i = 0; i < typeCode.member_count(); i++) {
            if(typeCode.is_member_key(i)) {
                String typeDef = oracletype(typeCode.member_type(i), false);
                if(null != typeDef) {
                   out.print(", ");
                   out.print("UPSERT_");
                   out.print(sqlName);
                   out.print(".");
                   out.print(typeCode.member_name(i).toUpperCase());
                }
            }
        }
        out.println(");");
        out.println("    COMMIT;");
        out.print("    RETURN UPSERT_");
        out.print(sqlName);
        out.print(".");
        out.print(sqlName);
        out.println("_ID;");
        out.println("END;");
        out.println("END;");
        out.println("/");

    }
    
    public static void main(String[] args) throws BadKind, Bounds {
        printSchema(System.out, ice.AlarmLimitTypeCode.VALUE, ice.AlarmLimitTopic.VALUE);
        printSchema(System.out, ice.AlertTypeCode.VALUE, ice.PatientAlertTopic.VALUE);
        printSchema(System.out, ice.AlertTypeCode.VALUE, ice.TechnicalAlertTopic.VALUE);
        printSchema(System.out, ice.DeviceIdentityTypeCode.VALUE, ice.DeviceIdentityTopic.VALUE);
        printSchema(System.out, ice.DeviceConnectivityTypeCode.VALUE, ice.DeviceConnectivityTopic.VALUE);
        printSchema(System.out, ice.NumericTypeCode.VALUE, ice.NumericTopic.VALUE);
    }
    public static void printSchema(PrintStream out, TypeCode typeCode, String topicString) throws BadKind, Bounds {
        String sqlName = topicToSql(topicString);
        printCreateTables(out, typeCode, sqlName);
    }
    public static String topicToSql(String topicString) {
        return topicString.toUpperCase().substring(0, Math.min(topicString.length(), 13));
    }
}
