package de.thedead2.progression_reloaded.util.logger;

import de.thedead2.progression_reloaded.util.ConfigManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

import java.util.Objects;


@Plugin(name = "MissingAdvancementFilter", category = Node.CATEGORY, elementType = Filter.ELEMENT_TYPE)
public class MissingAdvancementFilter extends AbstractFilter {

    @Override
    public Result filter(LogEvent event) {
        Message message = event.getMessage();
        if(Objects.equals(message.getFormat(), "Ignored advancement '{}' in progress file {} - it doesn't exist anymore?") && ConfigManager.DISABLE_ADVANCEMENTS.get()) {
            return Result.DENY;
        }
        else {
            return Result.NEUTRAL;
        }
    }
}
