import java

Block = java.type('net.minecraft.block.Block')
AbstractBlock = java.type('net.minecraft.block.AbstractBlock')
Material = java.type('net.minecraft.block.Material')
BlockItem = java.type('net.minecraft.item.BlockItem')
ItemGroup = java.type('net.minecraft.item.ItemGroup')
Registry = java.type('net.minecraft.util.registry.Registry')
Identifier = java.type('net.minecraft.util.Identifier')
ActionResult = java.type('net.minecraft.util.ActionResult')
LiteralText = java.type('net.minecraft.text.LiteralText')
FabricBlockSettings = java.type('net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings')
FabricItemSettings = java.type('net.fabricmc.fabric.api.item.v1.FabricItemSettings')
IncludeMethodFilter = java.type('dev.vriska.quiltlangpolyglot.IncludeMethodFilter')
ProxyFactory = java.type('javassist.util.proxy.ProxyFactory')

def onUsePythonBlock(block, state, world, pos, player, hand, hit):
    if not world.isClient:
        player.sendMessage(LiteralText('Hello from Python!'), False)

    return ActionResult.SUCCESS

def pythonBlockHandler(self, method, proceed, args):
    return onUsePythonBlock(self, *args)

def onInitialize():
    blockFactory = ProxyFactory()
    blockFactory.setSuperclass(Block)
    blockFactory.setFilter(IncludeMethodFilter(Block, ['onUse']))

    pythonBlockSettings = FabricBlockSettings.of(Material.METAL).strength(4)

    PYTHON_BLOCK = blockFactory.create([AbstractBlock.Settings], [pythonBlockSettings], pythonBlockHandler)

    Registry.register(Registry.BLOCK, Identifier('quilt_lang_polyglot', 'python_block'), PYTHON_BLOCK)
    Registry.register(Registry.ITEM, Identifier('quilt_lang_polyglot', 'python_block'), BlockItem(PYTHON_BLOCK, FabricItemSettings().group(ItemGroup.MISC)))
