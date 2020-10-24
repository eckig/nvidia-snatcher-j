package model;

import java.util.Arrays;
import java.util.Optional;

public enum Model
{
    RTX_3080_FE("3080_fe"),
    RTX_3090_FE("3090_fe");

    private final String mTag;

    Model(final String pTag)
    {
        mTag = pTag;
    }

    public static Optional<Model> forTag(final String pTag)
    {
        return Arrays.stream(Model.values()).filter(s -> s.mTag.equalsIgnoreCase(pTag)).findFirst();
    }
}
