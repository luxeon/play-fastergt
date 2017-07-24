package play.modules.gtengineplugin.gt_integration;

import java.util.Locale;

public interface MessageResolver {

    String resolve(Object key, Object... args);

    String resolveMessage(Locale locale, Object key, Object... args);

    boolean supportsNativeMessages(Locale locale);

    String translate(String text, Locale sourceLocale, Locale targetLocale);

}
