package net.minecraft.world;

public enum EnumDifficulty
{
    PEACEFUL(0, "options.difficulty.peaceful"),
    EASY(1, "options.difficulty.easy"),
    NORMAL(2, "options.difficulty.normal"),
    HARD(3, "options.difficulty.hard");
    public static final EnumDifficulty[] VALUES = values();
    private static final EnumDifficulty[] difficultyEnums = new EnumDifficulty[VALUES.length];
    private final int difficultyId;
    private final String difficultyResourceKey;

    private EnumDifficulty(int difficultyIdIn, String difficultyResourceKeyIn)
    {
        this.difficultyId = difficultyIdIn;
        this.difficultyResourceKey = difficultyResourceKeyIn;
    }

    public int getDifficultyId()
    {
        return this.difficultyId;
    }

    public static EnumDifficulty getDifficultyEnum(int p_151523_0_)
    {
        return difficultyEnums[p_151523_0_ % difficultyEnums.length];
    }

    public String getDifficultyResourceKey()
    {
        return this.difficultyResourceKey;
    }

    static {
        for (EnumDifficulty enumdifficulty : VALUES)
        {
            difficultyEnums[enumdifficulty.difficultyId] = enumdifficulty;
        }
    }
}
