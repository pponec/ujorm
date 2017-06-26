/*
 *  Copyright 2013-2014 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm.extensions;

import java.util.ArrayList;
import java.util.List;
import org.ujorm.Key;
import org.ujorm.Ujo;

/**
 * The help class to simple copy between Ujo objects.
 * @author Pavel Ponec
 */
public class KeyPairs<SRC extends Ujo, TRG extends Ujo> {

    private final List<PairItem> pairs = new ArrayList<>();
    /** Locked sign */
    private boolean locked;

    @SuppressWarnings("unchecked")
    public <V> void add(Key<? super SRC, V> source, Key<? super TRG, V> target) throws UnsupportedOperationException {
        checkLock();
        pairs.add(new PairItem(source, target));
    }

    private void checkLock() throws UnsupportedOperationException {
        if (locked) {
            throw new UnsupportedOperationException("The class is locked");
        }
    }

    public KeyPairs<SRC, TRG> lock() {
        locked = true;
        return this;
    }


    /** Copy target to a source */
    public void copyToTarget(SRC source, TRG target) {
        for (PairItem pairItem : pairs) {
            pairItem.copyToTarget(source, target);
        }
    }

    /** Copy source to a target. */
    public void copyToSource(SRC source, TRG target) {
        for (PairItem pairItem : pairs) {
            pairItem.copyToSource(target, source);
        }
    }

    // ------------- PAIR CLASS -------------

    private static final class PairItem<SRC extends Ujo, TRG extends Ujo, V> {

        private final Key<? super SRC, V> srcKey;
        private final Key<? super TRG, V> trgKey;

        public PairItem(Key<? super SRC, V> srcKey, Key<? super TRG, V> tgtKey) {
            this.srcKey = srcKey;
            this.trgKey = tgtKey;
        }

        /** Copy value to target */
        @SuppressWarnings("unchecked")
        private void copyToTarget(SRC source, TRG target) {
            final Object value = srcKey.of(source);
            trgKey.setValue(target, (V) value);
        }

        /** Copy value to source */
        @SuppressWarnings("unchecked")
        private void copyToSource(TRG target, SRC source) {
            final Object value = trgKey.of(target);
            srcKey.setValue(source, (V) value);
        }
    }

    // ----------- FACTORY -----------

    public static <SRC extends Ujo, TRG extends Ujo, V> KeyPairs<SRC,TRG> get() {
        return new KeyPairs<>();
    }


}