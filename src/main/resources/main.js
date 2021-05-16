const Material = Java.type('net.minecraft.block.Material');
const BlockItem = Java.type('net.minecraft.item.BlockItem');
const ItemGroup = Java.type('net.minecraft.item.ItemGroup');
const Registry = Java.type('net.minecraft.util.registry.Registry');
const Identifier = Java.type('net.minecraft.util.Identifier');
const ActionResult = Java.type('net.minecraft.util.ActionResult');
const LiteralText = Java.type('net.minecraft.text.LiteralText');
const FabricBlockSettings = Java.type('net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings');
const FabricItemSettings = Java.type('net.fabricmc.fabric.api.item.v1.FabricItemSettings');

/*const EXAMPLE_BLOCK = newBlock(FabricBlockSettings.of(Material.METAL).strength(4), {
  onUse: 'onUseExampleBlock'
});

function onUseExampleBlock(block, state, world, pos, player, hand, hit) {
  if (!world.isClient) {
    player.sendMessage(new LiteralText('Hello from JS!'), false);
  }

  return ActionResult.SUCCESS;
}*/

function onInitialize() {
  //Registry.register(Registry.BLOCK, new Identifier('js', 'example_block'), EXAMPLE_BLOCK);
  //Registry.register(Registry.ITEM, new Identifier('js', 'example_block'), new BlockItem(EXAMPLE_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
}
