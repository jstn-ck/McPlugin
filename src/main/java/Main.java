import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;


public class Main extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        getLogger().info(Class.class.toString() + " has been enabled.");
        ItemStack molotowCocktail = new ItemStack(Material.GLASS_BOTTLE);
        ItemMeta meta = molotowCocktail.getItemMeta();
        meta.setDisplayName("Molotow Cocktail");

        molotowCocktail.setItemMeta(meta);
        NamespacedKey key = new NamespacedKey(this, "molotow_cocktail");
        ShapedRecipe molotowCocktailRecipe = new ShapedRecipe(key, molotowCocktail);
        molotowCocktailRecipe.shape("   ", " M ", " B ");
        molotowCocktailRecipe.setIngredient('M', Material.MAGMA_BLOCK);
        molotowCocktailRecipe.setIngredient('B', Material.GLASS_BOTTLE);
        Bukkit.addRecipe(molotowCocktailRecipe);
        Bukkit.broadcastMessage("minecraft_version: 1.0");
    }

    @EventHandler
    public void onPlayerInteraction(PlayerInteractEvent e) {
        Bukkit.broadcastMessage("onPlayerInteraction");
        Bukkit.broadcastMessage(e.getAction().toString());
        if (e.getHand().equals(EquipmentSlot.HAND)) {
            Bukkit.broadcastMessage("mainhand");
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Player player = e.getPlayer();
                ItemStack inh = player.getInventory().getItemInMainHand();
                if (inh.getItemMeta().getDisplayName().equals("Molotow Cocktail")) {
                    Bukkit.broadcastMessage("item is cocktail");
                    playerThrowEvent(player);
                }
            }
        }
    }

    //@EventHandler
    //public void onItemHitObject(ProjectileHitEvent event) {
    //    Bukkit.broadcastMessage("OBJECT IS HIT");
    // }

    public void playerThrowEvent(Player player) {
        Bukkit.broadcastMessage("In throw method");
        ItemStack itemToThrow = player.getInventory().getItemInMainHand();
        ItemStack throwStack = new ItemStack(itemToThrow);
        throwStack.setAmount(1);
        Location playerEyeLocation = player.getEyeLocation();


        Item thrownItem = player.getWorld().dropItem(playerEyeLocation, throwStack);
        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
        thrownItem.setVelocity(player.getLocation().getDirection().multiply(0.6D));
        new BukkitRunnable() {
            @Override
            public void run() { //item break when on ground
                if (itemHitSurface(thrownItem)) {
                    Bukkit.broadcastMessage("item hit surface");
                    if(itemHitSurfaceInDirectionZ(thrownItem)) {
                        //flyDirectionOfThrownItem = thrownItem.flyDirection().equals(EAST); case switch(flyDirectionOfThrownItem)

                        Bukkit.broadcastMessage("item hit surface in direction Z");
                        Location locZBlock = thrownItem.getLocation();
                        locZBlock.setZ(locZBlock.getZ() - 1);
                        if(locZBlock.getBlock().getType() != Material.AIR) {
                            Bukkit.broadcastMessage("item flew in direction NORTH (-Z)");
                            molotowHitSurfaceAction(thrownItem, locZBlock);
                        } else {
                            locZBlock.setZ(locZBlock.getZ() + 2);
                            molotowHitSurfaceAction(thrownItem, locZBlock);
                            Bukkit.broadcastMessage("item flew in direction SOUTH (+Z)");
                        }
                    } else if (itemHitSurfaceInDirectionX(thrownItem)) {
                        Bukkit.broadcastMessage("item hit surface in direction X");
                        Location locXBlock = thrownItem.getLocation();
                        locXBlock.setX(locXBlock.getX() - 1);
                        if(locXBlock.getBlock().getType() != Material.AIR) {
                            Bukkit.broadcastMessage("item flew in direction WEST (-X)");
                            molotowHitSurfaceAction(thrownItem, locXBlock);
                        } else {
                            locXBlock.setX(locXBlock.getX() + 2);
                            molotowHitSurfaceAction(thrownItem, locXBlock);
                            Bukkit.broadcastMessage("item flew in direction EAST (+X)");
                        }

                    } else {
                        Bukkit.broadcastMessage("item hit on ground");
                        molotowHitSurfaceAction(thrownItem, thrownItem.getLocation());
                    }
                    this.cancel();
                }
            }

        }.runTaskTimer(this, 1L, 1L);

//        thrownItem.getLocation().getWorld().playEffect(thrownItem.getLocation(), Effect.MOBSPAWNER_FLAMES, 6);
//        thrownItem.remove();
    }

    private boolean itemHitSurfaceInDirectionX(Item thrownItem) {
        return thrownItem.getVelocity().getX() == 0 && !thrownItem.isOnGround();
    }

    private boolean itemHitSurfaceInDirectionZ(Item thrownItem) {
        return thrownItem.getVelocity().getZ() == 0 && !thrownItem.isOnGround();
    }

    private boolean itemHitSurface(Item thrownItem) {
        return thrownItem.getVelocity().getZ() == 0 || thrownItem.isOnGround() || thrownItem.getVelocity().getX() == 0;
    }

    private void createFallingFireSpread(Location loc, Item item, Double vectorMultiplicator, Double vectorMultiplicatorY) {
        BlockData blockData = Material.FIRE.createBlockData();

        FallingBlock fb = item.getWorld().spawnFallingBlock(loc, blockData);

        float x = (float) ((Math.random() + 0.1D) * vectorMultiplicator);
        float y = (float) ((Math.random() + 0.1D) * vectorMultiplicatorY);
        float z = (float) ((Math.random() + 0.1D) * vectorMultiplicator);

        fb.setVelocity(new Vector(x, y, z));

        fb.setDropItem(false);
    }

    private void molotowHitSurfaceAction(Item item, Location loc) {
        Bukkit.broadcastMessage("in hitSurfaceAction method");
        loc.getWorld().playEffect(loc, Effect.POTION_BREAK, 300);
        item.getLocation().getBlock().setType(Material.FIRE);
        //FIRE SPREAD

        for (Entity entity : item.getNearbyEntities(3, 3, 3)) {

            entity.setFireTicks(300);

        }

        createFallingFireSpread(loc, item, 0.4D, 0.4D);
        createFallingFireSpread(loc, item, 0.4D, 0.4D);
        createFallingFireSpread(loc, item, 0.4D, 0.4D);
        createFallingFireSpread(loc, item, 0.4D, 0.4D);
        createFallingFireSpread(loc, item, -0.4D, 0.4D);
        createFallingFireSpread(loc, item, -0.4D, 0.4D);
        createFallingFireSpread(loc, item, -0.4D, 0.4D);
        createFallingFireSpread(loc, item, -0.4D, 0.4D);

        item.remove();
    }

    @Override
    public void onDisable() {

    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("test")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.sendMessage("TestTestTest");
                Bukkit.broadcastMessage("minecraft_version: 1.0");
            } else {
                sender.sendMessage("You are not a player!");
                return true;
            }
            return true;
        }

        if (label.equalsIgnoreCase("version")) {
            Bukkit.broadcastMessage("minecraft_version: 1.0");
            return true;
        }
        return false;
    }
}