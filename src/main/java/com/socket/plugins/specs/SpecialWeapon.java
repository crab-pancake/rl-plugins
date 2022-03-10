package com.socket.plugins.specs;

enum SpecialWeapon {
    DRAGON_WARHAMMER("Dragon Warhammer", 13576, false),
    ARCLIGHT("Arclight", 19675, false),
    DARKLIGHT("Darklight", 6746, false),
    BANDOS_GODSWORD("Bandos Godsword", 11804, true),
    BANDOS_GODSWORD_OR("Bandos Godsword", 20370, true),
    DAWNBRINGER("Dawnbringer", 22516, true),
    DINHS_BULWARK("Dinh's Bulwark", 21015, false),
    VULNERABILITY("Vulnerability", 566, false),
    BARRELCHEST_ANCHOR("Barrelchest Anchor", 10887, true),
    DORGESHUUN_CROSSBOW("Bone crossbow", 8880, true),
    BONE_DAGGER("Bone dagger", 8872, true),
    BONE_DAGGER_P("Bone dagger p", 8874, true),
    BONE_DAGGER_PP("Bone dagger p+", 8876, true),
    BONE_DAGGER_S("Bone dagger p++", 8878, true);

    private final String name;
    private final int itemID;
    private final boolean damage;

    SpecialWeapon(String name, int itemID, boolean damage) {
        this.name = name;
        this.itemID = itemID;
        this.damage = damage;
    }

    public String getName() {
        return this.name;
    }

    public int getItemID() {
        return this.itemID;
    }

    public boolean isDamage() {
        return this.damage;
    }
}

