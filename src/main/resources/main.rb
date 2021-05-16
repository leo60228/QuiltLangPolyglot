Java.import 'net.minecraft.block.Block'
Java.import 'net.minecraft.block.AbstractBlock'
Java.import 'net.minecraft.block.Material'
Java.import 'net.minecraft.item.BlockItem'
Java.import 'net.minecraft.item.ItemGroup'
Java.import 'net.minecraft.util.registry.Registry'
Java.import 'net.minecraft.util.Identifier'
Java.import 'net.minecraft.util.ActionResult'
Java.import 'net.minecraft.text.LiteralText'
Java.import 'net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings'
Java.import 'net.fabricmc.fabric.api.item.v1.FabricItemSettings'
Java.import 'dev.vriska.quiltlangpolyglot.IncludeMethodFilter'
Java.import 'javassist.util.proxy.ProxyFactory'

def onUseRubyBlock(block, state, world, pos, player, hand, hit)
    if not world.isClient then
        player.sendMessage LiteralText.new('Hello from Ruby!'), false
    end

    ActionResult.SUCCESS
end

def onInitialize()
    blockFactory = ProxyFactory.new
    blockFactory.setSuperclass Block
    blockFactory.setFilter IncludeMethodFilter.new(['onUse'])

    rubyBlockSettings = FabricBlockSettings.of(Material.METAL).strength(4)

    rubyBlock = blockFactory.create([AbstractBlock.Settings], [rubyBlockSettings]) do |this, method, proceed, args|
        if method.getName == 'onUse' then
            onUseRubyBlock(this, *args)
        else
            proceed.invoke self, args
        end
    end

    Registry.register Registry.BLOCK, Identifier.new('quilt_lang_polyglot', 'ruby_block'), rubyBlock
    Registry.register Registry.ITEM, Identifier.new('quilt_lang_polyglot', 'ruby_block'), BlockItem.new(rubyBlock, FabricItemSettings.new.group(ItemGroup.MISC))
end
