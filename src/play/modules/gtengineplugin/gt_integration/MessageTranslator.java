package play.modules.gtengineplugin.gt_integration;

import java.util.Locale;

public interface MessageTranslator {

    String translate(Locale to, Object key, Object[] args);

    boolean supports(Locale locale);
}
