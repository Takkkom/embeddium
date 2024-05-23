package org.embeddedt.embeddium.api.options.structure;

import com.google.common.collect.ImmutableList;
import org.embeddedt.embeddium.impl.Embeddium;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.embeddedt.embeddium.api.OptionPageConstructionEvent;
import org.embeddedt.embeddium.api.options.OptionIdentifier;
import org.embeddedt.embeddium.impl.gui.options.OptionIdGenerator;

import java.util.List;

public class OptionPage {
    public static final OptionIdentifier<Void> DEFAULT_ID = OptionIdentifier.create(Embeddium.MODID, "empty");

    private final OptionIdentifier<Void> id;
    private final Component name;
    private final ImmutableList<OptionGroup> groups;
    private final ImmutableList<Option<?>> options;

    private static OptionIdentifier<Void> tryMakeId(Component name) {
        OptionIdentifier<Void> id;
        if(name.getContents() instanceof TranslatableContents translatableContents) {
            String key = translatableContents.getKey();
            if(name.getSiblings().isEmpty()) {
                // Detect our own tabs
                id = switch(key) {
                    case "stat.generalButton" -> StandardOptions.Pages.GENERAL;
                    case "sodium.options.pages.quality" -> StandardOptions.Pages.QUALITY;
                    case "sodium.options.pages.advanced" -> StandardOptions.Pages.ADVANCED;
                    case "sodium.options.pages.performance" -> StandardOptions.Pages.PERFORMANCE;
                    default -> OptionIdGenerator.generateId(key);
                };
            } else {
                id = OptionIdGenerator.generateId(key);
            }
        } else {
            id = OptionIdGenerator.generateId(name.getString());
        }
        if(id != null) {
            Embeddium.logger().debug("Guessed ID for legacy OptionPage '{}': {}", name.getString(), id);
            return id;
        } else {
            Embeddium.logger().warn("Id must be specified in OptionPage '{}'", name.getString());
            return DEFAULT_ID;
        }
    }

    @Deprecated
    public OptionPage(Component name, ImmutableList<OptionGroup> groups) {
        this(tryMakeId(name), name, groups);
    }

    public OptionPage(OptionIdentifier<Void> id, Component name, ImmutableList<OptionGroup> groups) {
        this.id = id;
        this.name = name;
        this.groups = collectExtraGroups(groups);

        ImmutableList.Builder<Option<?>> builder = ImmutableList.builder();

        for (OptionGroup group : this.groups) {
            builder.addAll(group.getOptions());
        }

        this.options = builder.build();
    }

    private ImmutableList<OptionGroup> collectExtraGroups(ImmutableList<OptionGroup> groups) {
        OptionPageConstructionEvent event = new OptionPageConstructionEvent(this.id, this.name);
        OptionPageConstructionEvent.BUS.post(event);
        List<OptionGroup> extraGroups = event.getAdditionalGroups();
        return extraGroups.isEmpty() ? groups : ImmutableList.<OptionGroup>builder().addAll(groups).addAll(extraGroups).build();
    }

    public OptionIdentifier<Void> getId() {
        return id;
    }

    public ImmutableList<OptionGroup> getGroups() {
        return this.groups;
    }

    public ImmutableList<Option<?>> getOptions() {
        return this.options;
    }

    public Component getName() {
        return this.name;
    }

}
