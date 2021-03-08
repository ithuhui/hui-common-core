package pers.hui.calcite;

import com.google.common.collect.Lists;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.Lex;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.hep.HepPlanner;
import org.apache.calcite.plan.hep.HepProgram;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelDistributionTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.rel.rel2sql.SqlImplementor;
import org.apache.calcite.rel.rules.CoreRules;
import org.apache.calcite.rel.rules.PruneEmptyRules;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.dialect.MysqlSqlDialect;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.util.SqlString;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Properties;

/**
 * <code>SqlParserTest</code>
 * <desc>
 * 描述：
 * <desc/>
 * <b>Creation Time:</b> 2021/2/25 16:14.
 *
 * @author Gary.Hu
 */
public class SqlParserTest {
    @Test
    public void parserTest() throws SqlParseException {
        SqlParser sqlParser = SqlParser.create("select * from emps where id = 1 limit 10 "
                , SqlParser.Config.DEFAULT.withLex(Lex.ORACLE)
        );
        SqlNode sqlNode = sqlParser.parseQuery();
        SqlString oracleSql = sqlNode.toSqlString(MysqlSqlDialect.DEFAULT);
        System.out.println(oracleSql);
    }

    @Test
    public void optimizer() throws Exception {
        String testSql = "select u.id as user_id, u.name as user_name, u.age as user_age\n" +
                "from users u\n" +
                "         join jobs j on u.id = j.id\n" +
                "where u.age > 30\n" +
                "  and u.age > 50\n" +
                "  and u.id is not null\n" +
                "order by user_id\n" +
                "limit 10 where u.age = 22 ";

        System.out.println("原始sql: " + testSql);
        // sql解析器
        SqlParser sqlParser = SqlParser.create(testSql
                , SqlParser.Config.DEFAULT.withLex(Lex.ORACLE)
        );

        SqlNode sqlNode = sqlParser.parseQuery();


        SqlSelect sqlNode1 = (SqlSelect) sqlNode;
        SqlNode from = sqlNode1.getFrom();
        System.out.println(from);

        SchemaPlus rootSchema = CalciteUtils.registerRootSchema();

        SqlTypeFactoryImpl factory = new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT);


        final FrameworkConfig frameworkConfig = Frameworks.newConfigBuilder()
                .parserConfig(SqlParser.Config.DEFAULT)
                .defaultSchema(rootSchema)
                .traitDefs(ConventionTraitDef.INSTANCE, RelDistributionTraitDef.INSTANCE)
                .build();

        CalciteCatalogReader calciteCatalogReader = new CalciteCatalogReader(
                CalciteSchema.from(rootSchema),
                CalciteSchema.from(rootSchema).path(null),
                factory,
                new CalciteConnectionConfigImpl(new Properties()));


        // 初始化规则优化器
        HepProgram hepProgram = HepProgram.builder()
//                .addRuleInstance(FilterJoinRule.FilterIntoJoinRule.FILTER_ON_JOIN)
//                .addRuleInstance(ReduceExpressionsRule.PROJECT_INSTANCE)\

                .addRuleInstance(CoreRules.AGGREGATE_REMOVE)
                .addRuleInstance(CoreRules.PROJECT_FILTER_TRANSPOSE)
                .addRuleInstance(CoreRules.MULTI_JOIN_BOTH_PROJECT)
                .addRuleInstance(CoreRules.MULTI_JOIN_LEFT_PROJECT)
                .addRuleInstance(CoreRules.MULTI_JOIN_OPTIMIZE)
                .addRuleInstance(CoreRules.MULTI_JOIN_OPTIMIZE_BUSHY)
                .addRuleInstance(CoreRules.AGGREGATE_JOIN_REMOVE)
                .addRuleInstance(CoreRules.AGGREGATE_JOIN_JOIN_REMOVE)
                .addRuleInstance(PruneEmptyRules.PROJECT_INSTANCE)
                .addMatchLimit(10)
                .build();
        HepPlanner planner = new HepPlanner(hepProgram);


        SqlValidator validator = SqlValidatorUtil
                .newValidator(SqlStdOperatorTable.instance(), calciteCatalogReader,
                        factory, SqlValidator.Config.DEFAULT);

        final RexBuilder rexBuilder = CalciteUtils.createRexBuilder(factory);
        final RelOptCluster cluster = RelOptCluster.create(planner, rexBuilder);

        SqlNode validated = validator.validate(sqlNode);

        final SqlToRelConverter.Config config = frameworkConfig.getSqlToRelConverterConfig();

        final SqlToRelConverter sqlToRelConverter = new SqlToRelConverter(new CalciteUtils.ViewExpanderImpl(),
                validator, calciteCatalogReader, cluster, frameworkConfig.getConvertletTable(), config);


        RelRoot root = sqlToRelConverter.convertQuery(validated, false, true);
        root = root.withRel(sqlToRelConverter.flattenTypes(root.rel, true));
        RelNode relRoot = root.rel;

        planner.setRoot(relRoot);
        RelNode relNode = planner.findBestExp();

        System.out.println("解析relNode");
        System.out.println(RelOptUtil.toString(relNode));
        System.out.println("===============================");

        RelToSqlConverter relToSqlConverter = new RelToSqlConverter(MysqlSqlDialect.DEFAULT);
        SqlImplementor.Result result = relToSqlConverter.visitRoot(relNode);
        SqlString resSql = result.asSelect().toSqlString(MysqlSqlDialect.DEFAULT);
        System.out.println(resSql);
    }


    @Test
    public void test() throws Exception {
        // schema 1
        SchemaInfo schemaInfo = new SchemaInfo();
        SchemaInfo.ColumnInfo columnInfo = new SchemaInfo.ColumnInfo();
        columnInfo.setName("ID");
        columnInfo.setColumnType(SqlTypeName.INTEGER);

        SchemaInfo.ColumnInfo columnInfo1 = new SchemaInfo.ColumnInfo();
        columnInfo1.setName("NAME");
        columnInfo1.setColumnType(SqlTypeName.CHAR);

        SchemaInfo.ColumnInfo columnInfo2 = new SchemaInfo.ColumnInfo();
        columnInfo2.setName("AGE");
        columnInfo2.setColumnType(SqlTypeName.INTEGER);

        schemaInfo.setTable("USERS");

        // schema 2
        SchemaInfo schemaInfo2 = new SchemaInfo();
        SchemaInfo.ColumnInfo columnInfo3 = new SchemaInfo.ColumnInfo();
        columnInfo3.setName("ID");
        columnInfo3.setColumnType(SqlTypeName.INTEGER);

        SchemaInfo.ColumnInfo columnInfo4 = new SchemaInfo.ColumnInfo();
        columnInfo4.setName("NAME");
        columnInfo4.setColumnType(SqlTypeName.CHAR);

        SchemaInfo.ColumnInfo columnInfo5 = new SchemaInfo.ColumnInfo();
        columnInfo5.setName("COMPANY");
        columnInfo5.setColumnType(SqlTypeName.CHAR);

        schemaInfo2.setTable("JOBS");
        ArrayList<SchemaInfo.ColumnInfo> columnInfos = Lists.newArrayList(columnInfo, columnInfo1, columnInfo2);
        ArrayList<SchemaInfo.ColumnInfo> columnInfos2 = Lists.newArrayList(columnInfo3, columnInfo4, columnInfo5);
        schemaInfo.setColumnInfoList(columnInfos);
        schemaInfo2.setColumnInfoList(columnInfos2);

        ArrayList<SchemaInfo> schemaInfos = Lists.newArrayList(schemaInfo, schemaInfo2);
        String testSql = "select u.id as user_id, u.name as user_name,  u.age as user_age\n" +
                "from users u\n" +
                "         join jobs j on u.id = j.id\n" +
                "where u.age > 30\n" +
                "  and u.age > 50\n" +
                "order by user_id\n" +
                "limit 10";

        System.out.println(testSql);

        String optimization = CalciteUtil.optimization(schemaInfos, testSql, Lex.ORACLE);

        System.out.println(optimization);
    }
}
