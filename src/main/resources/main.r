Block <- java.type('net.minecraft.block.Block')
AbstractBlock <- java.type('net.minecraft.block.AbstractBlock')
AbstractBlockSettingsArray <- java.type('net.minecraft.block.AbstractBlock$Settings[]')
ClassArray <- java.type('java.lang.Class[]')
Material <- java.type('net.minecraft.block.Material')
BlockItem <- java.type('net.minecraft.item.BlockItem')
ItemGroup <- java.type('net.minecraft.item.ItemGroup')
Registry <- java.type('net.minecraft.util.registry.Registry')
Identifier <- java.type('net.minecraft.util.Identifier')
ActionResult <- java.type('net.minecraft.util.ActionResult')
LiteralText <- java.type('net.minecraft.text.LiteralText')
FabricBlockSettings <- java.type('net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings')
FabricItemSettings <- java.type('net.fabricmc.fabric.api.item.v1.FabricItemSettings')
IncludeMethodFilter <- java.type('dev.vriska.quiltlangpolyglot.IncludeMethodFilter')
ProxyFactory <- java.type('javassist.util.proxy.ProxyFactory')
ArrayList <- java.type('java.util.ArrayList')

onUseRBlock <- function(block, state, world, pos, player, hand, hit) {
    if (!world$isClient) {
        player$sendMessage(new(LiteralText, "Hello from R!"), FALSE)
    }

    return(ActionResult$SUCCESS)
}

rBlockHandler <- function(self, method, proceed, args) {
    if (method$getName() == "onUse") {
        return(do.call(onUseRBlock, as.list(c(self, args))))
    } else {
        return(proceed$invoke(self, args))
    }
}

onInitialize <- function() {
    blockFactory <- new(ProxyFactory)
    blockFactory$setSuperclass(Block$class)

    filteredMethods <- new(ArrayList, 1)
    filteredMethods$add("onUse")
    blockFactory$setFilter(new(IncludeMethodFilter, filteredMethods))

    rBlockSettings <- FabricBlockSettings$of(Material$METAL)$strength(4)

    paramTypes <- new(ClassArray, 1)
    paramTypes[1] <- AbstractBlock$Settings$class

    params <- new(AbstractBlockSettingsArray, 1)
    params[1] <- rBlockSettings

    R_BLOCK = blockFactory$create(paramTypes, params, rBlockHandler)

    Registry$register(Registry$BLOCK, new(Identifier, "quilt_lang_polyglot", "r_block"), R_BLOCK)

    rItemSettings <- new(FabricItemSettings)["group(net.minecraft.item.ItemGroup)"](ItemGroup$MISC)

    blockItem <- new(BlockItem, R_BLOCK, rItemSettings)
    Registry$register(Registry$ITEM, new(Identifier, "quilt_lang_polyglot", "r_block"), blockItem)
}
