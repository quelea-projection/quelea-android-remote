package org.quelea.mobileremote.items;

/**
 * Translation line item used for in-app translation.
 * Created by Arvid on 2018-01-27.
 */

public class TranslationLine {

    public void setIgnoreWarning(boolean ignoreWarning) {
        this.ignoreWarning = ignoreWarning;
    }

    private boolean ignoreWarning = false;
    private String original;
    private String translation;
    private boolean hasChanged;
    private String lastSave;
    private String label;

    public String getOriginal() {
        return original;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        hasChanged = !translation.equals(lastSave);
        this.translation = translation;
    }

    public void setLastSave(String lastSave) {
        this.lastSave = lastSave;
    }

    public String getLabel() {
        return label;
    }

    public TranslationLine(String original, String translation, String label) {
        this.original = original;
        this.translation = translation;
        this.label = label;
        this.lastSave = translation;
    }

    public boolean isFinished() {
        return !(getOriginal().contains("%s") && !getTranslation().contains("%s"))
                && !getTranslation().isEmpty() && !(getOriginal().equals(getTranslation()) && !ignoreWarning);
    }

    public boolean hasChanged() {
        return hasChanged;
    }
}
