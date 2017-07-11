package play.modules.gtengineplugin.gt_integration;

import java.util.Locale;

public interface MessageResolver {

    String resolve(Object key, Object[] args);

    boolean supports(Locale locale);
}
