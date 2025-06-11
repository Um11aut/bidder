# Strategies
Each strategy utilizes the Strategies parameters. There is a builder for that in `strategies/builder/BidderStrategyParameters.java`.

Strategies are provided with such parameters:
- `greediness`: Enum representing how aggressively the strategy should bid (`WEAK`, `MEDIUM`, `STRONG`)
- `maxRounds`: Maximum number of rounds the strategy is allowed to bid (not the actual game limit, but useful for evaluation)
- `riskReward`: A ratio (risk vs. reward) guiding how cautious or bold the strategy should be

The core function is:
```java
OptionalInt nextBid(BidderState own, BidderContext ctx);
```
The own `BidderState` is the current state of the bidder - contains `quantity`, `cash`, etc.

The `BidderContext` contains the public state for all the bid parties.

## `BalancedBidderStrategy`
The `BalancedBidderStrategy` calculates bids based on available cash, a greed multiplier, and a weighted risk-reward ratio. 
```java
double greedMultiplier = switch (params.greediness()) {
    case STRONG -> 1.5;
    case MEDIUM -> 1.0;
    case WEAK -> 0.5;
};
```
It prioritizes bidding based on the proportion of reward in the risk-reward configuration, making bids more aggressive when reward outweighs risk.

To prevent overspending early, bids are capped to just under 50% of the initial total quantity, and are always clamped within available cash.

Bidding stops automatically after the configured maxRounds, if specified.

## `GodlikeBidderStrategy`
A reactive and assertive bidding strategy that adjusts its behavior based on the opponent’s previous bids. It blends predictive aggression with resource-aware constraints to maximize winning chances while maintaining balance.

Strategy Inputs
Uses standard strategy parameters from BidderStrategyParameters:
- `greediness`: Ignored by this strategy (behavior is predefined).
- `maxRounds`: Upper round limit (optional).
- `riskReward`: Ignored — strategy is not ratio-based.
 
### Core Logic:
If opponent bidding history exists, get their last round’s maximum bid:
```java
BidderHistoryUnit lastRound = ctx.getHistory().getLast();
Optional<Integer> otherBidOpt = lastRound.getMaxBidInRound(own.id());
```

If no opponent bid is found, bid aggressively:
```java
bidValue = (int) (ownCash * 0.7 + random.nextDouble() * ownCash * 0.1);
```
Applies multiple caps:
- Must be ≥ 1 and ≤ available cash.
- Must not exceed 50% of initial quantity (rounded):
```java
int godlikeMaxBidPerRoundQuantityCap = (int) (initialQuantity * 0.5);
```
Stops bidding when configured round limit is reached.

## `RandomBidderStrategy`
Just randomly selects things