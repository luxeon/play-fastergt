package play.modules.gtengineplugin;

public interface TemplateMessageSource {

    String get(Object key, Object... args);

    String getMessage(String locale, Object key, Object... args);

}
