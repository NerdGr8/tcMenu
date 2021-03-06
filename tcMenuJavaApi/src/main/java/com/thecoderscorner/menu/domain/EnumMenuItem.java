/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.domain;

import com.google.common.base.Objects;
import com.thecoderscorner.menu.domain.state.IntegerMenuState;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

import java.util.Collections;
import java.util.List;

/**
 * A menu item implementation that represents one of a known set of choices, the choices are stored as an integer
 * value, but each choice has a string representation as well.
 */
public class EnumMenuItem extends MenuItem<Integer> {
    private final List<String> enumEntries;

    public EnumMenuItem() {
        super("", -1, -1, null, false);
        // needed for serialisation
        enumEntries = Collections.emptyList();
    }

    public EnumMenuItem(String name, int id, int eepromAddress, String functionName, List<String> enumEntries, boolean readOnly) {
        super(name, id, eepromAddress, functionName, readOnly);
        this.enumEntries = enumEntries;
    }

    public List<String> getEnumEntries() {
        return enumEntries;
    }

    @Override
    public MenuState<Integer> newMenuState(Integer value, boolean changed, boolean active) {
        return new IntegerMenuState(changed, active, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnumMenuItem that = (EnumMenuItem) o;
        return  Objects.equal(enumEntries, that.enumEntries) &&
                Objects.equal(name, that.name) &&
                Objects.equal(id, that.id) &&
                Objects.equal(eepromAddress, that.eepromAddress) &&
                Objects.equal(functionName, that.functionName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(eepromAddress, name, id, enumEntries, functionName);
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }
}
