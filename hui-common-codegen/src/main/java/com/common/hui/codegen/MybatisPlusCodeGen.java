package com.common.hui.codegen;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

/**
 * <code>MybatisPlusGen</code>
 * <desc>
 * 描述：
 * <desc/>
 * Creation Time: 2019/12/7 15:44.
 *
 * @author Gary.Hu
 */
public class MybatisPlusCodeGen {
    /**
     * INFO
     */
    private static final String AUTH = "Gary.hu";
    private static final String[] TABLES = new String[]{"t_schedule_job_inst"};
    private static final String TABLE_PREFIX = "t_schedule";
    private static final String BASE_ENTITY_CLASS = "com.hui.cloud.common.model.BaseEntity";

    /**
     * DATABSE
     */
    private static final String URL = "jdbc:mysql://localhost:3306/hui_cloud_schedule?useUnicode=true&useSSL=false&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
    private static final String DRIVER_NAME = "com.mysql.cj.jdbc.Driver";
    private static final String SCHEMA = "public";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";

    /**
     * GENERATOR
     */
    private static final String PARENT_PACKAGE = "com.hui.cloud";
    private static final String ENTITY_PACKAGE = "entity";
    private static final String SERVICE_PACKAGE = "service";
    private static final String SERVICE_IMPL_PACKAGE = "service.impl";
    private static final String MAPPER_PACKAGE = "mapper";


    private static String outputDir;

    /**
     * <p>
     * 读取控制台内容
     * </p>
     */
    private static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append("请输入" + tip + "：");
        System.out.println(help.toString());
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotEmpty(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }

    public static void main(String[] args) {
        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 全局配置================================
        //console 获取路径
        String projectPath = System.getProperty("user.dir");
        outputDir = scanner("tip: " + projectPath + "/xxx (输出文件夹的相对路径,没有二级目录请填NULL)");
        if ("NULL".equals(outputDir.toUpperCase())) {
            outputDir = "";
        }
        if (outputDir.startsWith("/") || outputDir.startsWith("\\")) {
            outputDir = File.separator + outputDir;
        }
        String outputPath = projectPath + outputDir + "/src/main/java";
        System.out.println("文件最终输出路径：" + outputPath + "\n");
        GlobalConfig gc = new GlobalConfig()
                //生成文件输出目录
                .setOutputDir(outputPath)
                // 文件覆盖
                .setFileOverride(false)
                //不需要ActiveRecord特性的请改为false
                .setActiveRecord(true)
                //作者
                .setAuthor(AUTH)
                //是否打开输出目录
                .setOpen(false)
                //是否打开Mybatis二级缓存
                .setEnableCache(false)
                //实体属性 Swagger2 注解
                .setSwagger2(false)
                .setBaseResultMap(false)
                .setBaseColumnList(false)
                //由于使用了Druid暂时不支持LocalDateTime。只能转换回Date
                .setDateType(DateType.ONLY_DATE)
                // 设置实体命名 (%Entity -> UserEntity)
                .setEntityName(null)
                // 设置Mapper命名 (%Dao -> UserDao)
                .setMapperName(null)
                // 设置Mapper.xml命名 (%Dao -> UserDao.xml)
                .setXmlName(null)
                // 设置Service命名
                .setServiceName("%sService")
                .setServiceImplName(null)
                // 主键策略 1.AUTO 数据库自增 2.NONE 默认，雪花算法 3.INPUT 手动插入ID 4.ID_WORKER 全局唯一 5. UUID 6. ID_WORKER 全局唯一的字符串
                .setIdType(IdType.NONE)
                //XML 生成ResultMap
                .setBaseResultMap(true);


        // 数据源配置================================
        DataSourceConfig dsc = new DataSourceConfig()
                .setUrl(URL)
                .setSchemaName(SCHEMA)
                .setDriverName(DRIVER_NAME)
                .setUsername(USERNAME)
                .setPassword(PASSWORD);

        // 包配置================================
        String parentPackage = scanner("tip: 输出的包路径");
        String modulePackage = scanner("tip: 输出的包模块名称");
        System.out.println("包最终输出路径：" + parentPackage + "." + modulePackage + "\n");

        PackageConfig pc = new PackageConfig()
                // 父包名
                .setParent(parentPackage)
                // 模块名
                .setModuleName(modulePackage)
                // 实体类/service/mapper/xml/controller包名
                .setEntity(ENTITY_PACKAGE)
                .setMapper(MAPPER_PACKAGE)
                .setXml(null)
                .setService(SERVICE_PACKAGE)
                .setServiceImpl(SERVICE_IMPL_PACKAGE);

        // 策略配置================================
        StrategyConfig strategy = new StrategyConfig()
                //表名前缀
                .setTablePrefix()
                //需要生成的表
                .setInclude(TABLES)
                //表名->下划线转驼峰结构
                .setNaming(NamingStrategy.underline_to_camel)
                //列名->下划线转驼峰结构
                .setColumnNaming(NamingStrategy.underline_to_camel)
                .setEntityLombokModel(true)
                .setRestControllerStyle(true)
                // 建造者模式的Entity
                .setEntityBuilderModel(true)
                .setControllerMappingHyphenStyle(true)
                .setTablePrefix(TABLE_PREFIX)
                // 逻辑删除的字段名称
                .setLogicDeleteFieldName("deleted")
                //是否生成实体时，生成字段注解
                .setEntityTableFieldAnnotationEnable(true)
                .setTableFillList(
                        Arrays.asList(
                                new TableFill("create_time", FieldFill.INSERT),
                                new TableFill("create_user", FieldFill.INSERT),
                                new TableFill("modify_time", FieldFill.INSERT_UPDATE),
                                new TableFill("modify_user", FieldFill.INSERT_UPDATE),
                                new TableFill("deleted", FieldFill.INSERT)
                        )
                );


        // 自定义配置==========================
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        }.setFileOutConfigList(Collections.singletonList(
                // 自定义配置会被优先输出
                new FileOutConfig("/templates/mapper.xml.vm") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
                        String xmlOutPutPath = outputDir
                                + "/src/main/resources/mapper"
                                + File.separator
                                + pc.getModuleName()
                                + File.separator
                                + tableInfo.getEntityName()
                                + "Mapper"
                                + StringPool.DOT_XML;
                        return xmlOutPutPath;
                    }
                })
        );

        // 模板设置
        TemplateConfig templateConfig = new TemplateConfig();
        // 设置XML位置为NULL 由自定义模板位置决定
        templateConfig.setXml(null);


        // 配置准备 ===========================
        mpg.setPackageInfo(pc)
                .setDataSource(dsc)
                .setGlobalConfig(gc)
                .setCfg(cfg)
                .setStrategy(strategy)
                .setTemplate(templateConfig)
                .setTemplateEngine(new VelocityTemplateEngine())
                .execute();
    }
}
