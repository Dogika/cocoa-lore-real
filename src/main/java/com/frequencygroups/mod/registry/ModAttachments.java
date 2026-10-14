package com.frequencygroups.mod.registry;

import com.frequencygroups.mod.FrequencyGroups;
import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModAttachments {

    public static final String LINK_GROUP_NBT_KEY = FrequencyGroups.MODID + ":link_group";

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, FrequencyGroups.MODID);

    public static final Supplier<AttachmentType<Boolean>> HAS_KILLED_VILLAGER = ATTACHMENTS.register(
            "has_killed_villager",
            () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .build()
    );

    public static final Supplier<AttachmentType<Boolean>> HAS_KILLED_FAKE_VILLAGER = ATTACHMENTS.register(
            "has_killed_fake_villager",
            () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .build()
    );

    public static final Supplier<AttachmentType<Boolean>> VILLAGER_PAINTED = ATTACHMENTS.register(
            "villager_painted",
            () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .build()
    );

    public static final Supplier<AttachmentType<Integer>> FIREWALKER_TICKS = ATTACHMENTS.register(
            "firewalker_ticks",
            () -> AttachmentType.builder(() -> 0)
                    .build()
    );

    public static final Supplier<AttachmentType<Integer>> BLEEDING_STACKS = ATTACHMENTS.register(
            "bleeding_stacks",
            () -> AttachmentType.builder(() -> 0)
                    .build()
    );

    private ModAttachments() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}