package play.modules.gtengineplugin;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import play.Play;
import play.exceptions.JavaExecutionException;
import play.exceptions.TemplateCompilationException;
import play.exceptions.TemplateExecutionException;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Lang;
import play.inject.RequireInjection;
import play.mvc.Http;
import play.template2.GTJavaBase;
import play.template2.GTRenderingResult;
import play.template2.GTTemplateLocation;
import play.template2.GTTemplateLocationReal;
import play.template2.exceptions.*;
import play.templates.Template;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

@RequireInjection
public class GTTemplate extends Template {

    private final GTTemplateLocation templateLocation;
    private final GTJavaBase gtJavaBase;

    @Inject
    private static TemplateMessageSource messageSource;

    public GTTemplate(GTTemplateLocation templateLocation, GTJavaBase gtJavaBase) {
        this.templateLocation = templateLocation;
        this.gtJavaBase = gtJavaBase;
        this.name = templateLocation.relativePath;
    }

    public GTTemplate(GTTemplateLocation templateLocation) {
        this.templateLocation = templateLocation;
        this.gtJavaBase = null;
        this.name = templateLocation.relativePath;
    }

    @Override
    public void compile() {
        //Don't have to do anything here..
    }

    @Override
    protected String internalRender(Map<String, Object> args) {


        GTRenderingResult renderingResult = internalGTRender(args);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        renderingResult.writeOutput(out, "utf-8");

        try {
            return new String(out.toByteArray(), "utf-8");
        } catch ( UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public GTRenderingResult internalGTRender(Map<String, Object> args) {
        Http.Request currentResponse = Http.Request.current();
        if ( currentResponse != null) {
            args.put("_response_encoding", currentResponse.encoding);
        }
        args.put("play", new Play());
        args.put("messages", messageSource);
        args.put("lang", Lang.get());

        return renderGTTemplate(args);
    }

    protected GTJavaBase getGTTemplateInstance() {
        if ( gtJavaBase == null) {
            return TemplateLoader.getGTTemplateInstance((GTTemplateLocationReal)templateLocation);
        } else {
            return gtJavaBase;
        }
    }

    protected GTRenderingResult renderGTTemplate(Map<String, Object> args) {

        try {

            GTJavaBase gtTemplate = getGTTemplateInstance();
            Monitor monitor = MonitorFactory.start(this.name);
            try {
                gtTemplate.renderTemplate(args);
            } finally {
                monitor.stop();
            }
            return gtTemplate;

        } catch ( GTTemplateNotFoundWithSourceInfo e) {
            GTTemplate t = new GTTemplate(e.templateLocation);
            t.loadSource();
            throw new TemplateNotFoundException(e.queryPath, t, e.lineNo);
        } catch (GTCompilationExceptionWithSourceInfo e) {
            GTTemplate t = new GTTemplate(e.templateLocation);
            t.loadSource();
            throw new TemplateCompilationException( t, e.oneBasedLineNo, e.specialMessage);
        } catch (GTRuntimeExceptionWithSourceInfo e){
            GTTemplate t = new GTTemplate(e.templateLocation);
            t.loadSource();
            Throwable cause = e.getCause();
            throw new TemplateExecutionException( t, e.lineNo, cause.getMessage(), cause);
        } catch ( GTRuntimeException e) {
            Throwable cause = e.getCause();
            this.loadSource();
            if (cause != null) {
                throw new TemplateExecutionException(this, 0, cause.getMessage(), cause);
            } else {
                throw new TemplateExecutionException(this, 0, e.getMessage(), e);
            }
        } catch (GTAppClassException e) {
            throw new JavaExecutionException(Play.classes.getApplicationClass(e.className), e.lineNo, e.getCause());
        }

    }

    @Override
    public String render(Map<String, Object> args) {
        return internalRender(args);
    }
    
    public void loadSource() {
        if ( source == null) {
            source = templateLocation.readSource();
        }
    }
}
