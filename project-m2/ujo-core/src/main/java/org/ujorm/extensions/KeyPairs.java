/*
 *  Copyright 2007-2013 Pavel Ponec
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
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.Ujo;

/**
 * The help class to simple copy between Ujo objects.
 * @author Pavel Ponec
 */
public class KeyPairs<SRC extends Ujo, TRG extends Ujo> {

    private List<PairItem> pairs = new ArrayList<PairItem>();
    /** Locked sign */
    private boolean locked;

    public <V> void add(Key<SRC, V> source, Key<TRG, V> target) throws UnsupportedOperationException {
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

    public void copyToTarget(SRC src, TRG target) {
        for (PairItem pairItem : pairs) {
            pairItem.copyToTarget(src, target);
        }
    }

    public void copyToSource(TRG target, SRC src) {
        for (PairItem pairItem : pairs) {
            pairItem.copyToSource(target, src);
        }
    }

    // ------------- PAIR CLASS -------------

    private static final class PairItem<SRC extends Ujo, TRG extends Ujo, V> {

        private final Key<SRC, V> srcKey;
        private final Key<TRG, V> trgKey;
        private final boolean compositeSrc;
        private final boolean compositeTrg;

        public PairItem(Key<SRC, V> srcKey, Key<TRG, V> tgtKey) {
            this.srcKey = srcKey;
            this.trgKey = tgtKey;
            this.compositeSrc = srcKey.isComposite();
            this.compositeTrg = tgtKey.isComposite();
        }

        /** Copy value to target */
        private void copyToTarget(SRC source, TRG target) {
            final Object value = srcKey.of(source);
            if (compositeTrg) {
                ((CompositeKey) trgKey).setValue(target, value, true);
            } else {
                trgKey.setValue(target, (V) value);
            }
        }

        /** Copy value to source */
        private void copyToSource(TRG target, SRC source) {
            final Object value = trgKey.of(target);
            if (compositeSrc) {
                ((CompositeKey) srcKey).setValue(source, value, true);
            } else {
                srcKey.setValue(source, (V) value);
            }
        }
    }

    // ----------- FACTORY -----------

    public static <SRC extends Ujo, TRG extends Ujo, V> KeyPairs<SRC,TRG> get() {
        return new KeyPairs<SRC, TRG>();
    }


}
