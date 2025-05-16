package ipana.modules.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//RTE2025 DAMAGE CALCULATOR
public class DamageCalculator {


    public static void main(String[] args) {
        //23, 28
        print(ItemEnum.DIAMOND_SWORD, 5, 2, true);
    }

    public static void print(ItemEnum item, int sharpness, int strength, boolean block) {
        List<Float> damages = new ArrayList<>();
        addDamages(damages,null, 0, strength, false);
        addDamages(damages,null, 0, strength, true);

        addDamages(damages, null, sharpness, strength, false);
        addDamages(damages, null, sharpness, strength, true);

        addDamages(damages, item, 0, strength, false);
        addDamages(damages, item, 0, strength, true);

        addDamages(damages, item, sharpness, strength, false);
        addDamages(damages, item, sharpness, strength, true);

        damages.sort(Comparator.comparingDouble(f -> f));
        damages = damages.stream().distinct().toList();
        //1.0
        //0.5
        //18.25

        //1.0
        //0.75
        //9.625
        List<DuraResult> results = new ArrayList<>();
        for (int i = 0; i < damages.size(); i++) {
            List<float[]> combinations = combinations(i+1, damages);
            for (float[] combination : combinations) {
                if (combination.length != 5) {
                    continue;
                }
                float lastDamage = 0;
                float finalDamage = 0;
                int armorDamage = 0;
                System.out.println("--------------------------------");
                for (float damage : combination) {
                    float damageDiff = damage - lastDamage;
                    float noBlock = damageDiff;
                    if (block) {
                        damageDiff = (1.0F + damageDiff) * 0.5F;
                    }
                    float floatDamage = damageArmor(damageDiff);
                    System.out.println("NoBlock: "+noBlock+", Damage: "+damageDiff+", Armor: "+floatDamage);
                    armorDamage += (int)floatDamage;
                    finalDamage += damageDiff;
                    lastDamage = damage;
                }
                System.out.println("Durability: "+armorDamage);
                results.add(new DuraResult(combination, armorDamage, finalDamage));
            }
        }
        results.sort(Comparator.<DuraResult>comparingInt(result -> -result.dura).thenComparingInt(result -> result.damages.length));
        //results.sort(Comparator.<DuraResult>comparingDouble(result -> -result.finalDamage).thenComparingInt(result -> result.damages.length));

        /*for (int i = 0; i < Math.min(results.size(), results.size()); i++) {
            DuraResult duraResult = results.get(i);
            if (duraResult.damages.length != 5 || duraResult.dura <= 9) {
                continue;
            }
            System.out.println("--------------------------------");
            System.out.println("Final: "+duraResult.finalDamage);
            System.out.println("Durability: "+duraResult.dura);
            System.out.println("Damage Count: "+duraResult.damages.length);
            for (float damage : duraResult.damages) {
                System.out.println("Damage: "+damage);
            }
        }*/
    }

    private static void addDamages(List<Float> damages, ItemEnum item, int sharpness, int strength, boolean crit) {
        float strengthDamage = 1.3f * strength;
        float baseDamage = 1f;

        if (item != null) {
            baseDamage += item.damage;
        }

        baseDamage *= 1 + strengthDamage;
        float sharpDamage = sharpness * 1.25f;

        if (crit) {
            baseDamage *= 1.5f;
        }
        baseDamage += sharpDamage;
        if (!damages.contains(baseDamage)) {
            damages.add(baseDamage);
        }
    }

    private static float damageArmor(float damage) {
        damage = damage / 4.0F;

        if (damage < 1.0F)
        {
            damage = 1.0F;
        }
        return damage;
    }

    private static List<float[]> combinations(int combination, List<Float> damages) {
        if (combination == damages.size()) {
            List<float[]> combinations = new ArrayList<>(1);
            float[] values = new float[damages.size()];
            for (int i = 0; i < values.length; i++) {
                values[i] = damages.get(i);
            }
            combinations.add(values);
            return combinations;
        }
        int combinationSize = factorial(damages.size()) / factorial(damages.size() - combination) * factorial(combination);
        List<float[]> combinations = new ArrayList<>();

        iterate(0, 1, combination, combinations, damages);
        return combinations;
    }

    private static void iterate(int index, int combinationIndex, int combination, List<float[]> combinations, List<Float> damages, int... indices) {
        for (int a = index; a < damages.size(); a++) {
            if (combination == combinationIndex) {
                float[] values = new float[indices.length + 1];
                for (int i = 0; i < indices.length; i++) {
                    values[i] = damages.get(indices[i]);
                }
                values[indices.length] = damages.get(a);
                combinations.add(values);
                continue;
            }
            int[] newIndices = new int[indices.length + 1];
            System.arraycopy(indices, 0, newIndices, 0, indices.length);
            newIndices[indices.length] = a;
            iterate(a + 1, combinationIndex + 1, combination, combinations, damages, newIndices);
        }
    }

    private static int factorial(int number) {
        int result = 1;
        for (int i = 2; i <= number; i++) {
            result *= i;
        }
        return result;
    }

    public enum ItemEnum {
        DIAMOND_SWORD(7.0f),
        IRON_SWORD(6.0f),
        STONE_SWORD(5.0f),
        WOOD_SWORD(4.0F),
        GOLD_SWORD(4.0F);

        private float damage;

        ItemEnum(float damage) {
            this.damage = damage;
        }
    }

    public static class DuraResult {
        public DuraResult(float[] damages, int dura, float finalDamage) {
            this.damages = damages;
            this.dura = dura;
            this.finalDamage = finalDamage;
        }

        public float[] damages;
        public int dura;
        public float finalDamage;
    }
}
