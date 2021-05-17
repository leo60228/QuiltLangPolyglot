const Block = Java.type('net.minecraft.block.Block');
const AbstractBlock = Java.type('net.minecraft.block.AbstractBlock');
const Material = Java.type('net.minecraft.block.Material');
const BlockItem = Java.type('net.minecraft.item.BlockItem');
const ItemGroup = Java.type('net.minecraft.item.ItemGroup');
const Registry = Java.type('net.minecraft.util.registry.Registry');
const Identifier = Java.type('net.minecraft.util.Identifier');
const ActionResult = Java.type('net.minecraft.util.ActionResult');
const LiteralText = Java.type('net.minecraft.text.LiteralText');
const FabricBlockSettings = Java.type('net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings');
const FabricItemSettings = Java.type('net.fabricmc.fabric.api.item.v1.FabricItemSettings');
const IncludeMethodFilter = Java.type('dev.vriska.quiltlangpolyglot.IncludeMethodFilter');
const SynchronizedMethodHandler = Java.type('dev.vriska.quiltlangpolyglot.SynchronizedMethodHandler');
const ProxyFactory = Java.type('javassist.util.proxy.ProxyFactory');

function onUseJsBlock(block, state, world, pos, player, hand, hit) {
  if (!world.isClient) {
    player.sendMessage(new LiteralText('Hello from JS!'), false);
  }

  return ActionResult.SUCCESS;
}

function onInitialize() {
  const blockFactory = new ProxyFactory();
  blockFactory.setSuperclass(Block);
  blockFactory.setFilter(new IncludeMethodFilter(Block, ['onUse']));

  const jsBlockSettings = FabricBlockSettings.of(Material.METAL).strength(4);

  const JS_BLOCK = blockFactory.create([AbstractBlock.Settings], [jsBlockSettings], new SynchronizedMethodHandler((self, method, proceed, args) => {
    return onUseJsBlock(self, ...args);
  }));

  Registry.register(Registry.BLOCK, new Identifier('quilt_lang_polyglot', 'js_block'), JS_BLOCK);
  Registry.register(Registry.ITEM, new Identifier('quilt_lang_polyglot', 'js_block'), new BlockItem(JS_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
}
