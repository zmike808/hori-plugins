package net.runelite.client.plugins.basicapi.api;

import net.runelite.api.Client;
import net.runelite.api.widgets.WidgetItem;

import java.util.List;
import java.util.function.Predicate;

public abstract class Items
{
    protected abstract List<WidgetItem> all(Predicate<WidgetItem> filter);

    protected List<WidgetItem> all(Client client, String... names)
    {
        return all(Predicates.names(client, names));
    }

    protected List<WidgetItem> all(Client client, int... ids)
    {
        return all(Predicates.ids(client, ids));
    }

    protected WidgetItem first(Predicate<WidgetItem> filter)
    {
        return all(filter).stream().findFirst().orElse(null);
    }

    protected WidgetItem first(Client client, int... ids)
    {
        return first(Predicates.ids(client, ids));
    }

    protected WidgetItem first(Client client, String... names)
    {
        return first(Predicates.names(client, names));
    }

    protected boolean exists(Predicate<WidgetItem> filter)
    {
        return first(filter) != null;
    }

    protected boolean exists(Client client, String... name)
    {
        return first(client, name) != null;
    }

    protected boolean exists(Client client, int... id)
    {
        return first(client, id) != null;
    }

    protected int count(boolean stacks, Predicate<WidgetItem> filter)
    {
        return all(filter).stream().mapToInt(x -> stacks ? x.getQuantity() : 1).sum();
    }

    protected int count(Client client, boolean stacks, int... ids)
    {
        return all(client, ids).stream().mapToInt(x -> stacks ? x.getQuantity() : 1).sum();
    }

    protected int count(Client client, boolean stacks, String... names)
    {
        return all(client, names).stream().mapToInt(x -> stacks ? x.getQuantity() : 1).sum();
    }
}