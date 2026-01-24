package de.Maxr1998.modernpreferences.preferences.choice


class SingleFloatChoiceDialogPreference(
    key: String,
    items: List<SelectionItem<Float>>,
) : AbstractSingleChoiceDialogPreference<Float>(key, items) {
    override fun saveKey(key: Float) {
        commitFloat(key)
    }

    override fun loadKey(defaultValue: Float?): Float? {
        return if (hasValue()) getFloat(0f) else defaultValue
    }
}