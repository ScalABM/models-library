package actors

import com.typesafe.config.Config


class ZILiquidityMarketMakerConfig(val config: Config) extends RandomLiquiditySupplierConfig
  with RandomLiquidityDemanderConfig {


}

