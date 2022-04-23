package net.runelite.client.plugins.basicapi.api;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.plugins.basicapi.utils.Inventory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Predicate;

@Singleton
@Slf4j
public class Predicates
{

    @Inject
    private static Inventory inventory;

    @Inject
    private static LegacyInventoryAssistant legacyInventoryAssistant;

    @Inject
    private static Client client;

    public static <T extends WidgetItem> Predicate<T> names(Client client, String... names)
    {
        return t ->
        {

            if(client.getItemDefinition(t.getId()).getName() == null){
                return false;
            }


            for (String name : names)
            {
                if (client.getItemDefinition(t.getId()).getName().equalsIgnoreCase(name))
                {
                    return true;
                }
            }

            return false;
        };
    }

    public static <T extends WidgetItem> Predicate<T> ids(Client client, int... ids)
    {
        return t ->
        {
            for (int id : ids)
            {
                if (t.getId() == id)
                {
                    return true;
                }
            }

            return false;
        };
    }
}
