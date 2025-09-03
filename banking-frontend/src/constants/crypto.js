export const CRYPTO_ASSETS = [
  { id: "bitcoin",           sym: "BTC",  name: "Bitcoin" },
  { id: "ethereum",          sym: "ETH",  name: "Ethereum" },
  { id: "binancecoin",       sym: "BNB",  name: "BNB" },
  { id: "ripple",            sym: "XRP",  name: "XRP" },
  { id: "cardano",           sym: "ADA",  name: "Cardano" },
  { id: "solana",            sym: "SOL",  name: "Solana" },
  { id: "dogecoin",          sym: "DOGE", name: "Dogecoin" },
  { id: "polkadot",          sym: "DOT",  name: "Polkadot" },
  { id: "polygon",           sym: "MATIC",name: "Polygon" },
  { id: "litecoin",          sym: "LTC",  name: "Litecoin" },
  { id: "tron",              sym: "TRX",  name: "TRON" },
  { id: "avalanche-2",       sym: "AVAX", name: "Avalanche" },
  { id: "chainlink",         sym: "LINK", name: "Chainlink" },
  { id: "uniswap",           sym: "UNI",  name: "Uniswap" },
  { id: "stellar",           sym: "XLM",  name: "Stellar" },
  { id: "monero",            sym: "XMR",  name: "Monero" },
  { id: "okb",               sym: "OKB",  name: "OKB" },
  { id: "internet-computer", sym: "ICP",  name: "Internet Computer" },
  { id: "aptos",             sym: "APT",  name: "Aptos" },
  { id: "near",              sym: "NEAR", name: "NEAR" },
];

export const ID_BY_SYM = Object.fromEntries(CRYPTO_ASSETS.map(a => [a.sym, a.id]));
export const IDS_CSV = CRYPTO_ASSETS.map(a => a.id).join(",");
