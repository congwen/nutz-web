package org.nutz.web.maker;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.segment.CharSegment;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.util.Disks;
import org.nutz.log.Log;
import org.nutz.log.Logs;

/**
 * 在给定的路径下生产符合nutz-web应用的目录结构与部分文件
 * 
 * @author pw
 * 
 */
public class WebMaker {

    private static Log log = Logs.get();

    private WebMaker() {}

    /**
     * 在制定目录生成一个符合nutz-web项目的web工程
     * 
     * @param pc
     */
    public static void newProject(ProjectConf pc) {
        // 打印信息
        log.info("project config :");
        log.info("\n" + Json.toJson(pc));
        // 生成对应目录
        File root = new File(Disks.absolute(pc.pdir));
        Files.createDirIfNoExists(root);
        String pkgPath = "src/" + pc.pkg;
        mkDir(root, "src");
        mkDir(root, pkgPath);
        mkDir(root, pkgPath + "/module");
        mkDir(root, "conf");
        mkDir(root, "conf/ioc");
        mkDir(root, "test");
        mkDir(root, "test/" + pc.pkg);
        String rootPath = mkDir(root, "ROOT");
        mkDir(root, "ROOT/WEB-INF");
        // 生成对应文件
        mkFile(root, pkgPath + "/" + Strings.upperFirst(pc.pnm), "java", _project(pc));
        mkFile(root, pkgPath + "/" + Strings.upperFirst(pc.pnm) + "Launcher", "java", _launcher(pc));
        mkFile(root,
               pkgPath + "/" + Strings.upperFirst(pc.pnm) + "MainModule",
               "java",
               _mainModule(pc));
        if (!pc.modules.isEmpty()) {
            for (String mnm : pc.modules.keySet()) {
                String at = Strings.sBlank(pc.modules.get(mnm), mnm);
                mkFile(root,
                       pkgPath + "/module/" + Strings.upperFirst(mnm) + "Module",
                       "java",
                       _subModule(pc, mnm, at));
            }
        }
        mkFile(root, pkgPath + "/" + Strings.upperFirst(pc.pnm) + "Setup", "java", _setup(pc));
        mkFile(root, pkgPath + "/" + Strings.upperFirst(pc.pnm) + "Config", "java", _conf(pc));

        mkFile(root, "conf/ioc/" + pc.pnm, "js", _ioc(pc));
        mkFile(root, "conf/log4j", "properties", _log4j());
        mkFile(root, "conf/web", "properties", _webProperties(pc, rootPath));

        mkFile(root, "ROOT/WEB-INF/web", "xml", _webXML(pc));
        mkFile(root, "ROOT/index", "html", _indexJSP());
        mkFile(root, "ROOT/404", "html", _404JSP());
        // 结束
        log.info("project has been created");
    }

    public static String mkDir(File root, String path) {
        if (-1 != path.indexOf(".")) {
            path = path.replaceAll("\\.", "/");
        }
        log.info("mkdir  : " + path);
        String dir = root.getAbsolutePath() + "/" + path;
        File mdir = Files.createDirIfNoExists(dir);
        if (mdir != null) {
            return mdir.getAbsolutePath();
        }
        return null;
    }

    public static void mkFile(File root, String path, String type, String fcontent) {
        if (-1 != path.indexOf(".")) {
            path = path.replaceAll("\\.", "/");
        }
        String fnm = path + (Strings.isBlank(type) ? "" : "." + type);
        log.info("mkfile : " + fnm);
        String file = root.getAbsolutePath() + "/" + fnm;
        try {
            File f = Files.createFileIfNoExists(file);
            Files.write(f, fcontent);
        }
        catch (IOException e) {
            log.error(e);
        }
    }

    // 各个文件生成

    private static Segment _readTmpl(String tnm) {
        String tmpl = Streams.readAndClose(new InputStreamReader(WebMaker.class.getResourceAsStream(tnm)));
        CharSegment cs = new CharSegment(tmpl);
        return cs;
    }

    private static String _project(ProjectConf pc) {
        Segment cs = _readTmpl("project.java.tmpl");
        cs.add("pkg", pc.pkg);
        cs.add("pnm", Strings.upperFirst(pc.pnm));
        return cs.render().toString();
    }

    private static String _launcher(ProjectConf pc) {
        Segment cs = _readTmpl("launcher.java.tmpl");
        cs.add("pkg", pc.pkg);
        cs.add("pnm", Strings.upperFirst(pc.pnm));
        return cs.render().toString();
    }

    private static String _webProperties(ProjectConf pc, String rootPath) {
        Segment cs = _readTmpl("web.properties.tmpl");
        cs.add("app-root-dir", rootPath);
        cs.add("app-rs", "");
        cs.add("app-admin-port", pc.app_admin_port);
        cs.add("app-port", pc.app_port);
        return cs.render().toString();
    }

    private static String _log4j() {
        return _readTmpl("log4j.properties.tmpl").render().toString();
    }

    private static String _ioc(ProjectConf pc) {
        Segment cs = _readTmpl("ioc.js.tmpl");
        cs.add("pkg", pc.pkg);
        cs.add("pnm", Strings.upperFirst(pc.pnm));
        return cs.render().toString();
    }

    private static String _conf(ProjectConf pc) {
        Segment cs = _readTmpl("conf.java.tmpl");
        cs.add("pkg", pc.pkg);
        cs.add("pnm", Strings.upperFirst(pc.pnm));
        return cs.render().toString();
    }

    private static String _mainModule(ProjectConf pc) {
        Segment cs = _readTmpl("mainModule.java.tmpl");
        cs.add("pkg", pc.pkg);
        cs.add("pnm", Strings.upperFirst(pc.pnm));
        return cs.render().toString();
    }

    private static String _subModule(ProjectConf pc, String mnm, String at) {
        Segment cs = _readTmpl("subModule.java.tmpl");
        cs.add("pkg", pc.pkg);
        cs.add("at", at);
        cs.add("mnm", Strings.upperFirst(mnm));
        return cs.render().toString();
    }

    private static String _setup(ProjectConf pc) {
        Segment cs = _readTmpl("setup.java.tmpl");
        cs.add("pkg", pc.pkg);
        cs.add("pnm", Strings.upperFirst(pc.pnm));
        return cs.render().toString();
    }

    private static String _indexJSP() {
        return _readTmpl("index.jsp.tmpl").render().toString();
    }

    private static String _404JSP() {
        return _readTmpl("404.html.tmpl").render().toString();
    }

    private static String _webXML(ProjectConf pc) {
        Segment cs = _readTmpl("web.xml.tmpl");
        cs.add("pkg", pc.pkg);
        cs.add("pnm", Strings.upperFirst(pc.pnm));
        return cs.render().toString();
    }

}
