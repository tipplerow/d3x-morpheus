### Introduction

In 1952, [Harry Markowitz](https://en.wikipedia.org/wiki/Harry_Markowitz) published a seminal paper in the Journal of Finance 
titled **Portfolio Selection**, where he first introduced [Modern Portfolio Theory](https://en.wikipedia.org/wiki/Modern_portfolio_theory) 
(MPT). The theory introduced a rigorous framework that quantifies the **risk** and **return** trade-off when constructing 
portfolios of risky assets, and formalized the concept of **diversification** in mathematical terms. It suggests that 
rational investors seek to make investment decisions that **maximize portfolio return** for a defined level of **risk**.

Given an investment universe of risky assets, MPT addresses the problem of **sizing the positions** of each asset in the portfolio
so as to **maximize efficiency**. Moreover, it also introduces the concept of [systematic](https://en.wikipedia.org/wiki/Systematic_risk)
versus [non-systematic](http://www.investopedia.com/terms/u/unsystematicrisk.asp) risk, the latter of which can be diversified away
in a well proportioned portfolio. Systematic risk refers to general market wide risks such as interest rate risk, business recessions
or wars, while non-systematic risk (also known as specific risk) relates to the **idiosyncratic risks** associated with an individual
security.

Modern Portfolio Theory is still in widespread use today in professional investment management circles, and remains one of the
foundational frameworks used to build efficient risk adjusted portfolios. Markowitz was awarded the **Nobel Memorial Prize in Economic
Sciences** in 1990 for his work on MPT.

In this article we review some of the basic mathematics of **portfolio return** & **risk**, and then use the **Morpheus library**
to demonstrate how to apply this knowledge to construct efficient investment portfolios. To help build intuition, we begin
with two asset portfolios before moving onto more real-world examples. In particular, we consider a [Robo-Advisor](https://en.wikipedia.org/wiki/Robo-advisor)
portfolio of 6 broad based [Exchange Traded Funds](https://en.wikipedia.org/wiki/Exchange-traded_fund), and compare this to a
[Risk Parity](https://en.wikipedia.org/wiki/Risk_parity) construction as well as to a basic 60/40 Stock/Bond allocation.

### Risk & Return

MPT proposes that a rational investor makes decisions so as to **maximize investment return** for a given level of **risk**. While
return is an unambiguous concept and easily measured, risk or uncertainty is not necessarily so readily quantified. In the context of
MPT however, risk is defined as the **variance of the portfolio returns**, which is a function of the **variance and covarinace** of
the individual asset returns in the portfolio (more on this [later](#portfolio-risk)). Before we delve into risk, let us first consider
portfolio return.

### Portfolio Return

**Portfolio return** is simply the weighted sum of the returns on the individual assets in the portfolio, which can be expressed
as per the equation below, where \\(w_{i}\\) represents the **weight** (percentage of capital) of asset i, \\(r_{i}\\) the return
on asset i, and \\(R_{p}\\) the overall portfolio return.

$$ R_{p} = \sum_{i=1}^{n} w_{i} * r_{i} $$

In order to illustrate the validity of this expression, consider a two asset hypothetical portfolio. Let us assume we have $1000
of capital, and we have decided to invest in just two stocks (n=2), namely Amazon and Apple. For lack of a strong opinion, we
simply split our investment 50/50 between the two names, and then over a year we see Apple return 15% and Amazon returns 8%.
How much does our portfolio return?

Ignoring the above formula for the moment, we can easily calculate the profit from each $500 dollar investment, sum these profits
and then calculate the resulting return on our $1000 initial investment, which comes out to be 11.5%.  In general terms, the
**future value** `F` of some monetary amount can be related to its **present value** `P` and a **rate of return** `R` as follows:

$$ F = P (1 + R) $$

Since we are ultimately trying to calculate the portfolio return, we need to write this expression in terms of the **present
value and the return on the individual assets** in the portfolio. The expression below does just this, where \\(p_{i}\\) and
\\(r_{i}\\) represent the present value and return of individual assets respectively, and subscript `p` denotes the portfolio
value:

$$ F_{p} = \sum_{i=1}^{n} p_{i} (1 + r_{i}) \\ where \\ P_{p} = \sum_{i=1}^{n} p_{i} $$

Considering our **two asset example**, we can expand the equation to yield:

$$ \begin{align}
F_{p} &= p_{1} (1 + r_{1}) + p_{2} ( 1 + r_{2}) \\\\
F_{p} &= p_{1} + p_{2} + p_{1} r_{1} + p_{2} r_{2} \\\\
F_{p} &= P_{p} + p_{1} r_{1} + p_{2} r_{2} \\\\
\end{align} $$

We know that from our original future value equation we can express the return of the portfolio as:

$$ R_{p} = \frac{F_{p}}{P_{p}} - 1 $$

Therefore if we divide the prior equation for our two asset scenario by the present value of the portfolio we get an expression
for the portfolio return in terms of the individual asset returns, which is essentially the same as the weighted sum of the asset
returns as defined earlier.

$$ R_{p} = \frac{F_{p}}{P_{p}} - 1 = \frac{p_{1} r_{1} + p_{2} r_{2}}{P_{p}} = w_{1} r_{1} + w_{2} r_{2} $$

### Portfolio Risk

MPT defines portfolio risk in terms of the **variance** or **volatility** of its returns. This is conceptually consistent with intuition,
since high **variance** implies a high degree of **uncertainty**. Portfolio returns that are extremely volatile are not only emotionally
hard to stomach, but may force an investor to crystallize losses at a very inopportune time due to a requirement to gain access to capital.
Long term investors often think of risk in different terms, and in fact [Warren Buffet](https://en.wikipedia.org/wiki/Warren_Buffett), who
is arguably the greatest investor of all time, would probably not consider volatility as an appropriate metric. Instead, he would be more
likely to think in terms of the potential for **permanent loss of capital**.

Few of us have the investment acumen or long horizon of Warren Buffet, so for mere mortals, let us stick with return volatility
as our best measure of portfolio risk. While individual asset return volatility is simple to compute, calculating portfolio
level risk is less trivial as it involves understanding the **interaction of individual asset returns**. That is to say, no
two assets in a portfolio are likely to be 100% correlated, and therefore combining uncorrelated assets is bound to affect
risk in ways that need to be quantified. Let us begin with our definition of portfolio return variance which using the
[expectation operator](https://en.wikipedia.org/wiki/Expected_value) is just the usual expression for [variance](https://en.wikipedia.org/wiki/Variance).

$$ \sigma_{p}^2 = E[ ( R_{p} - \bar{R}_{p} )^2 ] $$

In this equation \\(R_{p}\\) represents the return generated by an instance of the portfolio while \\(\bar{R}_{p}\\) represents
the **average expected return** from this portfolio. To illustrate, consider a two asset configuration where we expand the above
expression to be in terms of the individual assets that make up the portfolio.

$$ \begin{align}
\sigma_{p}^2 &= E[ ( R_{p} - \bar{R_{p}})^2 ] \\\\
\sigma_{p}^2 &= E[ ( w_{1} r_{1} + w_{2} r_{2} - (w_{1} \bar{r_{1}} + w_{2}\bar{r_{2}}) )^2 ] \\\\
\sigma_{p}^2 &= E[ ( w_{1} ( r_{1} - \bar{r_{1}} ) + w_{2} ( r_{2} - \bar{r_{2}}))^2 ] \\\\
\sigma_{p}^2 &= E[ w_{1}^2 ( r_{1} - \bar{r_{1}} )^2 + w_{2}^2 ( r_{2} - \bar{r_{2}})^2 + 2 w_{1} w_{2} E[(r_{1} - \bar{r_{1}})(r_{2} - \bar{r_{2}})]] \\\\
\end{align} $$

Since the **asset weights are not stochastic in nature**, we can take them outside of the expectation operator to yield the following:

$$ \sigma_{p}^2 = w_{1}^2 E[( r_{1} - \bar{r_{1}})^2] + w_{2}^2 E[( r_{2} - \bar{r_{2}})^2] + 2 w_{1} w_{2} E[(r_{1} - \bar{r_{1}})(r_{2} - \bar{r_{2}})]] $$

It now becomes clear that the portfolio variance is a function of the **individual asset variances** as well as their
**covariance**, so we can write the same expression in a more concise manner as shown below. Notice that this expression
suggests that if we combine risky assets in a portfolio that have a **negative covariance**, the overall portfolio **risk will
be reduced**. This is essentially diversification quantified, and it is a central principle of MPT.

$$ \begin{align}
\sigma_{p}^2 &= w_{1}^2 \sigma_{1}^2 + w_{2}^2 \sigma_{2}^2 + 2 w_{1} w_{2} Cov(r_{1}, r_{2}) \\\\
\sigma_{p}^2 &= \sum_{i=1}^{2} \sum_{j=1}^{2} w_{i} w_{j} Cov(r_{i}, r_{j}) \\\\
\end{align} $$

While the above derivation is for a 2 asset portfolio, this expression can be generalized to an N asset portfolio and
written in **matrix form** as below. The `w` term represents an `nx1` vector of asset weights, and capital sigma represents
the `nxn` covariance matrix of asset returns, the diagonal elements of which are the individual asset return variances.

$$ \sigma_{p}^2 = w^T \Sigma w $$

On the assumption that portfolio risk and return are related, it is often useful to consider how much **investment return** a
given allocation generates per **unit of risk**. There are several ways of measuring this, which is the topic of the next section.

### Sharpe Ratio

The [Sharpe Ratio](https://en.wikipedia.org/wiki/Sharpe_ratio) is a widely used performance metric in finance and is named
after Nobel Laureate [William F. Sharpe](https://en.wikipedia.org/wiki/William_F._Sharpe) who first developed it. The ratio
is basically the average return earned in **excess** of the **risk free rate** per unit of volatility, and is therefore a
standardized way of expressing **risk adjusted return**. Mathematically, it is defined as follows (where \\(R_{p}\\)
represents portfolio return, \\(\bar{R_{f}}\\) is the risk free return and \\(\sigma_{p}\\) is portfolio risk):

$$ S_{p} = \frac{E[ R_{p} - \bar{R_{f}}]}{\sigma_{p}}  $$

A Sharpe Ratio can be computed *ex-ante* based on **forecasts** of expected risk and return, or otherwise as an *ex-post*
measure based on realized returns. Given that the return measure (the stuff we like) is in the numerator and the risk measure
(the stuff we don't like) is in the denominator, the higher the Sharpe Ratio the better.

While it is very widely used, the Sharpe Ratio is not without its detractors. One of the often quoted grievances with the
measure is that it treats **upside volatility** equally with **downside volatility**. This may be reasonable in a long / short
portfolio, but perhaps less so in a long-only portfolio (i.e. no shorting constraint). In order to address this, a
variation of the Sharpe Ratio exists called the [Sortino Ratio](https://en.wikipedia.org/wiki/Sortino_ratio) which
only includes returns below a certain threshold when computing volatility.

Another potential concern is that the Sharpe Ratio does not distinguish between **systematic** and **non-systematic** risk,
the latter of which can be diversified away in a well balanced portfolio. The [Treynor Ratio](https://en.wikipedia.org/wiki/Treynor_ratio)
attempts to address this by estimating the systematic risk only, and using that as the denominator in the above expression.

Finally, one of the trickiest issues with the Sharpe Ratio is scaling it to different time horizons. For example, how
do you compute an annualized Sharpe from daily returns? Most techniques scale risk and return on the assumption that
returns are **normally distributed**, however it is well known that asset returns are not normal and exhibit excess
**kurtosis** and often **skewness**. For more details on some of the challenges in computing Sharpe Ratios, I highly
recommend a paper by Andrew W. Lo titled [The Statistics of Sharpe Ratios](http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.460.3450&rep=rep1&type=pdf).

### Examples

Now that we know how to calculate **portfolio return** and **risk**, let us consider how we can apply this knowledge, and
more importantly, demonstrate how we can use Modern Portfolio Theory to construct an efficient investment portfolio of
risky assets.  The examples in the following sections leverage the Morpheus data source adapter for **Yahoo Finance**
more details of which can be found [here](../../providers/yahoo). The library is available on **Maven Central** and can
therefore be added to your build tool of choice:

```xml
<dependency>
    <groupId>com.zavtech</groupId>
    <artifactId>morpheus-yahoo</artifactId>
    <version>${VERSION}</version>
</dependency>
```

### Two Assets

In order to develop some intuition for MPT, consider a **two asset** equity portfolio where we have already convinced ourselves
that we want to buy Apple and Amazon, but we are not sure how much of our capital to invest in each. We obviously do not know
what the future may bring, but let us look back to see how these assets performed historically on the (poor) assumption that it
may help influence our decision. The plot below shows the cumulative returns of both securities over the past year. With the benefit
of hindsight of course, you would have invested all your capital in Apple as it outperformed Amazon by some margin. Looking forward
however, the  performance of these stocks could very well be reversed, so we should probably spread our bets across the two.

<p align="center">
    <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_0.png"/>
</p>

The code to generate this plot is as follows:

<?prettify?>
```java
var end = LocalDate.now();
var start = end.minusYears(1);
var tickers = Array.of("AAPL", "AMZN");

var yahoo = new YahooFinance();
var cumReturns = yahoo.getCumReturns(start, end, tickers);
cumReturns.applyDoubles(v -> v.getDouble() * 100d);

Chart.create().withLinePlot(cumReturns, chart -> {
    chart.title().withText("Cumulative Asset Returns");
    chart.subtitle().withText("Range: (" + start + " to" + end + ")");
    chart.plot().axes().domain().label().withText("Date");
    chart.plot().axes().range(0).label().withText("Return");
    chart.plot().axes().range(0).format().withPattern("0.00'%';-0.00'%'");
    chart.show();
});
```
How we choose to **allocate** our investment capital between Apple and Amazon will affect how much return we earn, and how much
volatility we experience. As investors, we will ultimately need to make some **educated guesses** about the future prospects for
these assets, but before we do that, it is useful to consider the **spectrum of prior outcomes** given the benefit of **hindsight**.
In order to do this, we can generate a large number of **random portfolios** and then use realized asset returns and volatility to
study how the simulation portfolios would have performed.

#### Step 1 - Create Random Portfolios

To study the **risk & return characteristics** of our two asset portfolio, we are going to **generate 10,000 random portfolios**
where we invest different amounts in Apple and Amazon ranging from 0% to 100% of our capital in one asset and the remainder in the
other. The function below returns a `DataFrame` of random portfolio weights in N assets that sum to 1 in all cases, and therefore
each portfolio represents a **fully invested** scenario. The row keys of this frame are labelled `P0`, `P1` to `PM` and the instrument
tickers are used for the column keys.

<?prettify?>
```java
/**
 * A function that generates N long only random portfolios with weights that sum to 1
 * @param count     the number of portfolios / rows in the DataFrame, M
 * @param tickers   the security tickers to include, size N
 * @return          the frame of MxN random portfolios, 1 per row, labelled P0, P1 etc...
 */
DataFrame<String,String> randomPortfolios(int count, Iterable<String> tickers) {
    var rowKeys = Range.of(0, count).map(i -> "P" + i);
    var weights = DataFrame.ofDoubles(rowKeys, tickers);
    weights.applyDoubles(v -> Math.random());
    weights.rows().forEach(row -> {
        var sum = row.stats().sum();
        row.applyDoubles(v -> {
            var weight = v.getDouble();
            return weight / sum;
        });
    });
    return weights;
}
```

#### Step 2 - Calculate Risk & Return

Given a `DataFrame` of random portfolios, the next step is to calculate the risk & return associated with each allocation,
which requires estimates of asset **returns**, **variance** and **covariance**. Realized values for these quantities can be
used to assess historical performance, or forecasts can be used to model future possible outcomes. In this article we
avoid forecasts at all costs, and only consider historical performance.

Recall from earlier sections that portfolio return and risk can be computed using the following expressions.

$$ R_{p} = \sum_{i=1}^{n} w_{i} * r_{i} $$

$$ \sigma^2 = w^T \Sigma w $$

We will need to perform these calculations throughout the examples in this article, so as a result, the convenience function
below has been implemented to do just this. This function expects a `DataFrame` of portfolio weights in the same format as
that generated by `randomPortfolios()`, as well as the end date of the 1 year historical window with which to load the asset
returns.

The code uses the Morpheus API to compute the covariance matrix of the **daily returns**, which it then **annualizes** by
multiplying each element by 252 (on the assumption there are **252 trading days in the year**). Since the look-back window
defaults to 1 year, there is no need to annualize the returns. Finally, a boolean argument can be used to indicate whether
the resulting `DataFrame` should include a column containing the **Sharpe ratio** of each portfolio (note we assume that the
**risk free rate** is zero here, which is not entirely accurate but unimportant for this exercise).

<?prettify?>
```java
/**
 * Returns a DataFrame containing risk, return and sharpe ratio for portfolios calculated over 1-year
 * @param portfolios    the DataFrame of portfolio weights, one row per portfolio configuration
 * @param endDate       the end date for the 1-year period to compute risk & return
 * @param sharpe        if true, include a column with the Sharpe ratio for each portfolio
 * @return              the DataFrame with risk, return and sharpe
 */
DataFrame<String,String> calcRiskReturn(DataFrame<String,String> portfolios, LocalDate endDate, boolean sharpe) {
    var yahoo = new YahooFinance();
    var tickers = portfolios.cols().keyArray();
    var range = Range.of(endDate.minusYears(1), endDate);
    var dayReturns = yahoo.getDailyReturns(range, tickers);
    var cumReturns = yahoo.getCumReturns(range, tickers);
    var sigma = dayReturns.cols().stats().covariance().applyDoubles(x -> x.getDouble() * 252);
    var assetReturns = cumReturns.rows().last().map(DataFrameRow::toDataFrame).get();
    var riskReturn = DataFrame.ofDoubles(portfolios.rows().keyArray(),
        sharpe ? Array.of("Risk", "Return", "Sharpe") : Array.of("Risk", "Return")
    );
    portfolios.rows().forEach(row -> {
        var weights = row.toDataFrame();
        var portReturn = weights.dot(assetReturns.transpose()).data().getDouble(0, 0);
        var portVariance = weights.dot(sigma).dot(weights.transpose()).data().getDouble(0, 0);
        riskReturn.data().setDouble(row.key(), "Return", portReturn * 100d);
        riskReturn.data().setDouble(row.key(), "Risk", Math.sqrt(portVariance) * 100d);
        if (sharpe) {
            riskReturn.data().setDouble(row.key(), "Sharpe", portReturn / Math.sqrt(portVariance));
        }
    });
    return riskReturn;
}
```

#### Step 3 - Simulate Apple + Amazon

Using the above functions, we can now generate 10,000 random portfolios involving different allocations to Apple
and Amazon, and compute their risk & return characteristics based on historical asset returns. The resulting `DataFrame`
can be plotted on a scatter chart with portfolio risk as the **domain axis** and portfolio return as the **range axis**.
The resulting plot for one simulation of 10,000 portfolios is shown below, and is followed by the code to generate it.

<p align="center">
    <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_1.png"/>
</p>

<?prettify?>
```java
var count = 10000;
var endDate = LocalDate.now();
var tickers = Array.of("AAPL", "AMZN");
var portfolios = randomPortfolios(count, tickers);
var riskReturn = calcRiskReturn(portfolios, endDate, false);
Chart.create().withScatterPlot(riskReturn, false, "Risk", chart -> {
    chart.title().withText("Risk / Return Profiles For AAPL+AMZN Portfolios");
    chart.subtitle().withText(count + " Portfolio Combinations Simulated");
    chart.plot().axes().domain().label().withText("Portfolio Risk");
    chart.plot().axes().domain().format().withPattern("0.00'%';-0.00'%'");
    chart.plot().axes().range(0).label().withText("Portfolio Return");
    chart.plot().axes().range(0).format().withPattern("0.00'%';-0.00'%'");
    chart.show();
});
```

There are a few notable observations about the plot as follows:

* It appears to be impossible to create a portfolio with a risk lower than about 15.7% volatility.
* If you were willing to accept 16% risk, there are two portfolios, one of which has a much better return than the other.
* Much beyond 17% risk, it appears returns suffer tremendously due to overweight in one of the assets.
* The best possible return appears to be about 37% at a risk level just north of 17%.

A **rational investor** clearly wants to be on the upper part of this curve for a **given level of risk**. For example,
at 17% volatility, you could either have achieved 23.5% return with a portfolio on the lower segment of the curve
or close to 37% on the upper part. Which one do you prefer?

The upper part of the curve is referred to as the [Efficient Frontier](https://en.wikipedia.org/wiki/Efficient_frontier),
and moreover, there exists a special case on this curve often referred to as the **Markowitz Portfolio**, which has the
highest risk adjusted return of all possible candidates. It is essentially the **mean-variance optimal portfolio**
and can be calculated by maximizing the utility defined by the objective function below, subject to whatever constraints
you may impose (in this case we assume **long only** and **fully invested**):

$$ U_{p} = w^T r - w^T \Sigma w $$

The \\(w\\) and \\(r\\) terms represent `nx1` vectors of asset **weights** and **returns** respectively, while capital
**sigma** represents the `nxn` asset covariance matrix. This objective function is essentially saying we like return
and do not like risk, and our goal is to select an `nx1` vector of asset weights that maximizes this expression given
our assumption for asset returns and covariance. In this example, we are looking back at a historical scenario so we can
calculate returns and the covariance based on realized market prices. In reality, we need to make **judgements about the
future**, and therefore are required to **estimate future returns and covariance**, which is where things can get tricky.

Calculating the **Markowitz Portfolio** given constraints (as in this case where we impose a long only fully invested
constraint such that the elements of \\(w\\) are all positive and sum to 1) is a [quadratic optimization](https://en.wikipedia.org/wiki/Quadratic_programming)
problem that requires appropriate software and is beyond the scope of this article. A good commercial package
to consider is [MOSEK](http://docs.mosek.com/8.0/javafusion/case-studies-portfolio.html), or for an Open Source
solution you may consider [OptaPlanner](https://www.optaplanner.org/).

### Asset Selection

Modern Portfolio Theory is fundamentally about **sizing positions** of risky assets in a portfolio, it is not
about asset selection. Having said that, it can be useful to compare the risk / return profiles of portfolios
constructed from different risky assets. In the prior example we had already decided that we wanted to invest
in Apple and Amazon, but were there better two asset portfolio combinations we should have considered? The plot
below, followed by the code that generated it, is essentially an extension of the prior example, but in this
case we generate multiple 10,000 portfolio combinations with different asset constituents to see how they compare.

<p align="center">
    <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_2.png"/>
</p>

It is clear from the chart that the 5 two asset portfolios in this example have very different risk / return
profiles, with the Apple / Amazon combination being the most risky, but certainly having some of the highest
returns. With that being said, if your investment objective was to achieve around a 15.0% return (very
aspirational in a world of zero interest rates), your best bet would be to choose the VTI / BND combination
as it appears to have the **potential** to generate this return for less than 5% risk. Compare this to some of the other
portfolio combinations for which you need to accept a much **higher level of risk** to achieve the **same return**.

<?prettify?>
```java
//Define portfolio count, investment horizon
var count = 10000;
var endDate = LocalDate.now();

var results = Array.of(
    Array.of("VTI", "BND"),
    Array.of("AAPL", "AMZN"),
    Array.of("GOOGL", "BND"),
    Array.of("ORCL", "KO"),
    Array.of("VWO", "VNQ")
).map(v -> {
    var tickers = v.getValue();
    var portfolios = randomPortfolios(count, tickers);
    var riskReturn = calcRiskReturn(portfolios, endDate, false);
    var label = String.format("%s+%s", tickers.getValue(0), tickers.getValue(1));
    return riskReturn.cols().replaceKey("Return", label);
});

var first = results.getValue(0);
Chart.create().<Integer,String>withScatterPlot(first, false, "Risk", chart -> {
    for (int i=1; i<results.length(); ++i) {
        chart.plot().<String>data().add(results.getValue(i), "Risk");
        chart.plot().render(i).withDots();
    }
    chart.plot().axes().domain().label().withText("Portfolio Risk");
    chart.plot().axes().domain().format().withPattern("0.00'%';-0.00'%'");
    chart.plot().axes().range(0).label().withText("Portfolio Return");
    chart.plot().axes().range(0).format().withPattern("0.00'%';-0.00'%'");
    chart.title().withText("Risk / Return Profiles of Various Two Asset Portfolios");
    chart.subtitle().withText(count + " Portfolio Combinations Simulated");
    chart.legend().on().right();
    chart.show();
});
```

### Multiple Assets

So far we have limited our example portfolios to two assets in order to help develop the intuition behind Modern
Portfolio Theory. In reality however, real world portfolios are likely to include more assets, although exactly
how many are required to achieve a reasonable level of diversification is open to debate. Consider an investable
universe of 6 securities represented by broad based low cost ETFs that serve as reasonable proxies for major asset
classes. The table below summarizes these candidates.

| Ticker | Name                                     | Provider                                                                    |
|--------|------------------------------------------|-------------|---------------------------------------------------------------|
| VWO    | Vanguard FTSE Emerging Markets ETF       | Vanguard    | [Details](https://finance.yahoo.com/quote/VWO/profile?p=VWO)  |
| VNQ    | Vanguard REIT ETF                        | Vanguard    | [Details](https://finance.yahoo.com/quote/VNQ/profile?p=VNQ)  |
| VEA    | Vanguard FTSE Developed Markets ETF      | Vanguard    | [Details](https://finance.yahoo.com/quote/VEA/profile?p=VEA)  |
| DBC    | PowerShares DB Commodity Tracking ETF    | Powershares | [Details](https://finance.yahoo.com/quote/DBC/profile?p=DBC)  |
| VTI    | Vanguard Total Stock Market ETF          | Vanguard    | [Details](https://finance.yahoo.com/quote/DBC/profile?p=VTI)  |
| BND    | Vanguard Total Bond Market ETF           | Vanguard    | [Details](https://finance.yahoo.com/quote/BND/profile?p=BND)  |

To get a sense of how the risk / return profiles of portfolios evolve as we include more assets, we can generate 10,000
random portfolios first with 2 assets, then 3 and all the way to 6. In the case of two assets, we expect to see our
rotated parabola, but what happens when you have more degrees of freedom in the portfolio? The plot below is the answer.
As we go beyond two assets, the scatter becomes more pronounced, and while risk is generally reduced due to the fact that
the assets are not perfectly correlated, return also suffers somewhat. Having said that, the **Efficient Frontiers** of
the more diversified portfolios appear to provide a better risk / return trade off than the two asset portfolio.

<p align="center">
    <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_3.png"/>
</p>

The code to generate the above plot of 5 lots of 10,000 portfolios with various assets is as follows:

<?prettify?>
```java
var count = 10000;
var endDate = LocalDate.of(2017, 9, 29);
var results = Array.of(
    Array.of("VWO", "VNQ"),
    Array.of("VWO", "VNQ", "VEA"),
    Array.of("VWO", "VNQ", "VEA", "DBC"),
    Array.of("VWO", "VNQ", "VEA", "DBC", "VTI"),
    Array.of("VWO", "VNQ", "VEA", "DBC", "VTI", "BND")
).map(v -> {
    var tickers = v.getValue();
    var portfolios = randomPortfolios(count, tickers);
    var riskReturn = calcRiskReturn(portfolios, endDate, false);
    var label = String.format("%s Assets", tickers.length());
    return riskReturn.cols().replaceKey("Return", label);
});

var first = results.getValue(0);
Chart.create().<Integer,String>withScatterPlot(first, false, "Risk", chart -> {
    for (int i=1; i<results.length(); ++i) {
        chart.plot().<String>data().add(results.getValue(i), "Risk");
        chart.plot().render(i).withDots();
    }
    chart.plot().axes().domain().label().withText("Portfolio Risk");
    chart.plot().axes().domain().format().withPattern("0.00'%';-0.00'%'");
    chart.plot().axes().range(0).label().withText("Portfolio Return");
    chart.plot().axes().range(0).format().withPattern("0.00'%';-0.00'%'");
    chart.title().withText("Risk / Return Profiles of Portfolios With Increasing Assets");
    chart.subtitle().withText(count + " Portfolio Combinations Simulated");
    chart.legend().on().bottom();
    chart.show();
});
```

### Robo-Advisor

A fairly recent innovation in the investment management space relates to what are called [Robo-Advisors](https://en.wikipedia.org/wiki/Robo-advisor),
which are essentially online investment solutions aimed mostly at retail investors. They are called Robo-Advisors
because they automate the construction of portfolios using software based on an investor's risk appetite and their
investment objective, which they assess by posing a number of questions via their website. There are already many
players in this space, and most of them construct well-balanced portfolios consisting of 5-7 assets, all of which
are **broad based and low cost** [Exchange Traded Funds](https://en.wikipedia.org/wiki/Exchange-traded_fund).

To avoid any suggestion that I am endorsing or recommending these services, I am consciously avoiding naming names, but
I visited the website of one of the larger advisors and proceeded to complete the questionnaire after which it proposed
the portfolio in the table below. The purpose of this discussion is not to make any judgement on how good this portfolio
is versus other potential investments, but really to get a sense of how efficient the proposed allocations are from a
risk / return  stand point.

| Ticker | Name                                | Weight  | Provider     | Details                                                        |
|--------|-------------------------------------|---------|--------------|----------------------------------------------------------------|
| VTI    | Vanguard Total Stock Market ETF     |    35%  | Vanguard     | [Details](https://finance.yahoo.com/quote/DBC/profile?p=VTI)   |
| VEA    | Vanguard FTSE Developed Markets ETF |    21%  | Vanguard     | [Details](https://finance.yahoo.com/quote/VEA/profile?p=VEA)   |
| VWO    | Vanguard FTSE Emerging Markets ETF  |    16%  | Vanguard     | [Details](https://finance.yahoo.com/quote/VWO/profile?p=VWO)   |
| VTEB   | Vanguard Tax-Exempt Bond ETF        |    15%  | Vanguard     | [Details](https://finance.yahoo.com/quote/VTEB/profile?p=VTEB) |
| VIG    | Vanguard Dividend Appreciation ETF  |     8%  | Vanguard     | [Details](https://finance.yahoo.com/quote/VIG/profile?p=VIG)   |
| XLE    | Energy Select Sector SPDR ETF       |     5%  | State Street | [Details](https://finance.yahoo.com/quote/XLE/profile?p=XLE)   |

#### Random Portfolios

Ignoring the proposed weightings for the moment, consider generating 10,000 **long-only fully invested random portfolios**
involving these assets, and then computing the resulting equity curves. This will give us a sense of the various scenarios we can
generate with this asset universe, and in particular allow us to understand the **degree of dispersion** in outcomes. The plot
below illustrates 10,000 equity curves based on the **past 1 year returns**, suggesting a return spread ranging from approximately
2.5% all the way to 18.15%, which is pretty enormous.

<p align="center">
    <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_4.png"/>
</p>

The code to generate the above plot is as follows:

<?prettify?>
```java
var portfolioCount = 10000;
var range = Range.of(LocalDate.now().minusYears(1), LocalDate.now());
var tickers = Array.of("VTI", "BND", "VWO", "VTEB", "VIG", "XLE");
var portfolios = randomPortfolios(portfolioCount, tickers);
var performance = getEquityCurves(range, portfolios);
Chart.create().withLinePlot(performance.applyDoubles(v -> v.getDouble() * 100d), chart -> {
    chart.title().withText(portfolioCount + " Equity Curves (Past 1 Year Returns)");
    chart.subtitle().withText("Robo-Advisor Universe: VTI, BND, VWO, VTEB, VIG, XLE");
    chart.plot().axes().domain().label().withText("Date");
    chart.plot().axes().range(0).label().withText("Portfolio Return");
    chart.plot().axes().range(0).format().withPattern("0.00'%';-0.00'%'");
    chart.show();
});
```

This example uses a convenience function called `getEquityCurves()` as shown below, and this generates a `DataFrame` of cumulative
portfolio returns for each of our 10,000 random portfolios. The input `DataFrame` of asset weights is of `mxn` dimensions where `m` is
the number of portfolios and `n` the number of assets. The resulting `DataFrame` has `txm` dimensions where `t` is the number of dates,
and the `m` columns are labelled `P0`, `P1` through to `PM`.

<?prettify?>
```java
/**
 * Calculates equity curves over a date range given a frame on initial portfolio weight configurations
 * @param range         the date range for historical returns
 * @param portfolios    MxN DataFrame of portfolio weights, M portfolios, N assets
 * @return              the cumulative returns for each portfolio, TxM, portfolios labelled P0, P1 etc...
 */
DataFrame<LocalDate,String> getEquityCurves(Range<LocalDate> range, DataFrame<Integer,String> portfolios) {
    var yahoo = new YahooFinance();
    var tickers = portfolios.cols().keyArray();
    var cumReturns = yahoo.getCumReturns(range.start(), range.end(), tickers);
    var colKeys = Range.of(0, portfolios.rowCount()).map(i -> "P" + i);
    return DataFrame.ofDoubles(cumReturns.rows().keyArray(), colKeys, v -> {
        var totalReturn = 0d;
        for (int i=0; i<portfolios.colCount(); ++i) {
            var weight = portfolios.data().getDouble(v.colOrdinal(), i);
            var assetReturn = cumReturns.data().getDouble(v.rowOrdinal(), i);
            totalReturn += (weight * assetReturn);
        }
        return totalReturn;
    });
}
```

#### Proposed Portfolio

Given the **proposed portfolio weights**, we can use the past 1 year of returns for the assets in question to assess what the
risk and return of this portfolio would have been had we invested a year ago. The code below performs this analysis and suggests
that the portfolio returned `15.1%` for `6.71%` risk for the year up to 29-Sep-2017, which is pretty phenomenal and implies a
2 [Sharpe](#sharpe-ratio) portfolio. The code to generate these results is as follows:

<?prettify?>
```java
var endDate = LocalDate.of(2017, 9, 29);
var tickers = Array.of("VTI", "VEA", "VWO", "VTEB", "VIG", "XLE");
var portfolio = DataFrame.of(tickers, String.class, columns -> {
    columns.add("Weights", Array.of(0.35d, 0.21d, 0.16d, 0.15d, 0.08d, 0.05d));
});

var riskReturn = calcRiskReturn(portfolio.transpose(), endDate, false);
IO.println(String.format("Portfolio Return: %s", riskReturn.data().getDouble(0, "Return")));
IO.println(String.format("Portfolio Risk: %s", riskReturn.data().getDouble(0, "Risk")));
```

To get a sense of how **efficient** the proposed portfolio is relative to other potential **long only** configurations, we generate
100,000 random portfolios using the same technique presented earlier, and then proceed to plot these on a risk / return scatter
chart as shown below. The proposed portfolio, which is represented by the **red dot**, does appear to be reasonably efficient,
and may also suggest that this particular Robo-Advisor's future expectations are not that different from the past year.

<p align="center">
    <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_5.png"/>
</p>

The code to generate this plot is as follows:

<?prettify?>
```java
var count = 100000;
var endDate = LocalDate.of(2017, 9, 29);
var tickers = Array.of("VTI", "VEA", "VWO", "VTEB", "VIG", "XLE");
var weights = Array.of(0.35d, 0.21d, 0.16d, 0.15d, 0.08d, 0.05d);
var portfolios = randomPortfolios(count, tickers);
portfolios.rowAt("P0").applyDoubles(v -> weights.getDouble(v.colOrdinal()));
var riskReturn = calcRiskReturn(portfolios, endDate, false);
var chosen = riskReturn.rows().select("P0").cols().replaceKey("Return", "Chosen");

//Plot the results using a scatter plot
Chart.create().withScatterPlot(riskReturn.cols().replaceKey("Return", "Random"), false, "Risk", chart -> {
    chart.title().withText("Risk / Return Profile For Wealthfront Portfolio");
    chart.subtitle().withText(count + " Portfolio Combinations Simulated");
    chart.plot().<String>data().add(chosen, "Risk");
    chart.plot().render(1).withDots(10);
    chart.plot().style("Chosen").withColor(Color.RED);
    chart.plot().style("Random").withColor(Color.LIGHT_GRAY);
    chart.plot().axes().domain().label().withText("Portfolio Risk");
    chart.plot().axes().domain().format().withPattern("0.00'%';-0.00'%'");
    chart.plot().axes().range(0).label().withText("Portfolio Return");
    chart.plot().axes().range(0).format().withPattern("0.00'%';-0.00'%'");
    chart.legend().on().right();
    chart.show();
});
```
One may wonder why the **proposed portfolio** is not exactly on the **Efficient Frontier**, and there are several explanations
for this. The first is that the proposed weights are for a **forward looking portfolio**, and our example is using **past 1 year
returns** to test its efficiency. Secondly, there are an infinite number of ways of estimating an **ex-ante** covariance matrix,
which may involve **exponentially smoothing** returns (perhaps using different **half-lives** to estimate diagonal versus
off-diagonal terms), and perhaps applying shrinkage to off-diagonal terms to improve stability. In our example, we have the
**benefit of hindsight** and simply compute an **ex-post** covariance matrix, doing nothing fancy at all. Finally, the Robo-Advisor
may impose risk constraints (such as capping any one position to be no more than 35% of the portfolio perhaps), which may
penalize the strategy versus a less constrained instance, but which may be a very sensible compromise.

#### Best & Worst Cases

The previous section established that the **proposed portfolio** was reasonably efficient in the context of the past 1 year
of returns. In this section, we further consider its relative efficiency by looking at the equity curve of the **best**
and **worst** portfolios that we find in our 100,000 random candidates based on their [Sharpe Ratios](#sharpe-ratio).

The plot below shows that the **proposed portfolio** actually outperformed what is labelled as the **Best** portfolio in pure
**return space** (15.17% vs 14.04%), but not in **risk-adjusted terms** (Sharpe of 2.26 vs 2.58). That is, it yields a lower
return per unit of risk than the **best** portfolio, and the fact that it outperformed in return space is just a fluke. The
**worst** portfolio is a genuine shocker, and in fact spent much of the past year in a downtrend before bouncing back starting
in August. The risk, return and Sharpe ratios for these three portfolios is as follows:

<div class="frame"><pre class="frame">
  Index    |     Risk      |    Return     |    Sharpe    |
-----------------------------------------------------------
 Proposed  |   6.71244753  |  15.17524545  |  2.26076187  |
     Best  |   5.42662686  |  14.04032432  |  2.58730233  |
    Worst  |  10.63697035  |   3.38364529  |  0.31810235  |
</pre></div>

<p align="center">
    <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_6.png"/>
</p>

The code to generate this plot is as follows, and leverages the `getEquityCurves()` function discussed earlier.

<?prettify?>
```java
var portfolioCount = 100000;
var endDate = LocalDate.of(2017, 9, 29);
var range = Range.of(endDate.minusYears(1), endDate);
var tickers = Array.of("VTI", "VEA", "VWO", "VTEB", "VIG", "XLE");
var proposedWeights = Array.of(0.35d, 0.21d, 0.16d, 0.15d, 0.08d, 0.05d);
var portfolios = randomPortfolios(portfolioCount, tickers);
portfolios.rowAt("P0").applyDoubles(v -> proposedWeights.getDouble(v.colOrdinal()));
var riskReturn = calcRiskReturn(portfolios, endDate, true).rows().sort(false, "Sharpe");
var candidates = portfolios.rows().select("P0",
    riskReturn.rows().first().get().key(),
    riskReturn.rows().last().get().key()
);

var equityCurves = getEquityCurves(range, candidates).cols().mapKeys(col -> {
    switch (col.ordinal()) {
        case 0: return "Proposed";
        case 1: return "Best";
        case 2: return "Worst";
        default: return col.key();
    }
});

Chart.create().withLinePlot(equityCurves.applyDoubles(v -> v.getDouble() * 100d), chart -> {
    chart.title().withText("Best/Worst/Chosen Equity Curves (Past 1 Year Returns)");
    chart.subtitle().withText("Robo-Advisor Universe: VTI, VEA, VWO, VTEB, VIG, XLE");
    chart.plot().axes().domain().label().withText("Date");
    chart.plot().axes().range(0).label().withText("Return");
    chart.plot().axes().range(0).format().withPattern("0.00'%';-0.00'%'");
    chart.plot().style("Proposed").withColor(Color.BLACK).withLineWidth(1.5f);
    chart.plot().style("Best").withColor(Color.GREEN.darker().darker());
    chart.plot().style("Worst").withColor(Color.RED);
    chart.legend().on();
    chart.show();
});
```

### Parity Portfolios

While **mean-variance optimality** theoretically provides the best possible risk adjusted returns to an investor, the technique
is predicated on **accurate forecasts** of expected asset returns, variance and covariance. The future is of course highly uncertain
and therefore any forecasts will be associated with significant **estimation error**. It is therefore worth considering other sizing
techniques that are far **simpler** to implement, and do not require accurate forecasts of future asset returns. In this section
we look at **parity portfolios**, and specifically consider **equal capital weighting** and **equal risk weighting** of positions,
the latter of which is referred to as [Risk Parity](https://en.wikipedia.org/wiki/Risk_parity).

The most naive allocation technique would be **equal capital weighting**, which requires no forecasts of any kind. Considering
the Robo-Advisor portfolio discussed in the [prior section](#robo-advisor) which invests in 6 assets, equal allocations would
simply be 16.6667% per asset (1/6 th). **Equal risk weighting** is a little more involved. In order to calculate equal risk
weights, we first need to **decompose portfolio risk** into asset specific contributions.

#### Risk Decomposition

[Earlier](#portfolio-risk) we established that **asset return correlations** affect risk in a non-trivial way, and therefore a
simple **linear decomposition** of risk is not entirely straightforward. Ideally we would like an equation similar to that for
portfolio return which we [established](#portfolio-return) is the weighted sum of the individual asset returns. We can write such
an expression for risk by introducing a new term we call **marginal contribution** to risk, or \\(MC\\) as follows:

$$ \sigma_{p} = \sum_{i=1}^{n} w_{i} MC_{i} $$

Marginal contribution to risk represents the **change in portfolio risk for a small change in the asset weight**, and is
defined by the expression below. This definition suggests that \\(\sigma_{p}\\) depends on a derivative of \\(\sigma_{p}\\)
which introduces non-linearity due to asset correlations.

$$ MC_{i} = \frac{\partial \sigma_{p}}{\partial w_{i}}  $$

In order to build **intuition** for the \\(MC\\) term, consider a portfolio of **two assets** for which we [earlier](#portfolio-risk)
defined the overall variance via the expression below. By taking the square root of both sides, we get an expression for risk in terms
of volatility.

$$ \begin{align}
\sigma_{p}^2 &= w_{1}^2 \sigma_{1}^2 + w_{2}^2 \sigma_{2}^2 + 2 w_{1} w_{2} Cov(r_{1}, r_{2}) \\\\
\sigma_{p} &= (w_{1}^2 \sigma_{1}^2 + w_{2}^2 \sigma_{2}^2 + 2 w_{1} w_{2} Cov(r_{1}, r_{2}))^\frac{1}{2} \\\\
\end{align} $$


To compute the marginal contribution to risk of the **first asset**, we need to differentiate this expression with respect to
\\(w_1\\) which using the [Chain Rule](https://en.wikipedia.org/wiki/Chain_rule) yields the following result:

$$ \begin{align}
\frac{\partial \sigma_{p}}{\partial w_{1}} &= \frac{1}{{2 \sigma_{p}}} (2 w_{1} \sigma_{1}^2 + 2 w_{2} Cov(r_{1}, r_{2})) \\\\
&= \frac{1}{\sigma_{p}} (w_{1} \sigma_{1}^2 + w_{2} Cov(r_{1}, r_{2})) \\\\
&= \frac{1}{{\sigma_{p}}} (w_{1} Cov(r_{1}, r_{1}) + w_{2} Cov(r_{1}, r_{2})) \\\\
&= \frac{1}{\sigma_{p}} \sum_{j=1}^{2} w_{j} Cov(r_{1}, r_{j}) \\\\
\end{align} $$

The general equation for calculating the **marginal contribution** to risk of asset \\(i\\) in an N asset portfolio is as follows:

$$ MC_{i} = \frac{\partial \sigma_{p}}{\partial w_{i}} = \frac{1}{\sigma_{p}}\sum_{j=1}^{n} w_{j} Cov(r_{i}, r_{j}) $$



#### Risk Parity

Now that we can compute the marginal contribution to risk for each asset, the problem becomes one of selecting asset weights such
that each position in the portfolio **contributes an equal proportion to overall risk**. This is essentially an **optimization
problem** that can be solved relatively easily through software. It should be noted that this approach still requires a degree of
**forecasting skill** because forward looking variance and covariance needs to be estimated, however it is not unreasonable to argue
that the **estimation error** associated with variance is lower than that for returns.

For the purpose of illustration, we can take a slightly more **naive approach** to building a risk parity portfolio, and simply take
into account individual asset variance while ignoring covariance. On the assumption that we wish to build a fully invested portfolio
**without leverage**, the asset weight for a **naive risk parity strategy** can be defined as follows:

$$ w_{i} = \frac{1}{k \sigma_{i}} \\ where \\ k = \sum_{i=1}^{n} \sigma_{i}^{-1} $$

The function below provides a generic implementation of this equation for some arbitrary universe, and produces a `DataFrame` of
naive risk parity weights based on asset variances over some historical window. How naive these weights are

<?prettify?>
```java
/**
 * Returns naive risk parity weights for an investment universe
 * @param tickers       the security ticker symbols
 * @param dateRange     the date range for asset returns
 * @return              the DataFrame of risk parity weights
 */
public DataFrame<String,String> getRiskParityWeights(Iterable<String> tickers, Range<LocalDate> dateRange) {
    var yahoo = new YahooFinance();
    var returns = yahoo.getDailyReturns(dateRange, tickers);
    var variance = returns.cols().stats().variance().colAt(0).toArray();
    var volatility = variance.mapToDoubles(v -> Math.sqrt(252d * v.getDouble()));
    var k = volatility.mapToDoubles(v -> 1d / v.getDouble()).stats().sum().doubleValue();
    var riskParityWeights = volatility.mapToDoubles(v -> 1d / (v.getDouble() * k));
    return DataFrame.ofDoubles("RiskParity", tickers).applyDoubles(v -> {
        return riskParityWeights.getDouble(v.colOrdinal());
    });
}
```

#### Example

In order to develop a sense of the relative efficiency of parity portfolios, consider the **Robo-Advisor** proposed allocations
discussed [earlier](#robo-advisor). That portfolio invested in 6 low-cost ETFs, so we can use this same investment universe to
construct both equal capital weighted and equal risk weighted portfolios to see where they lie relative to the **efficient
frontier**. Equal capital weights are trivial to calculate, and for the risk parity weights, we apply the naive construction
illustrated in the prior section, which in code is as follows:

<?prettify?>
```java
var yahoo = new YahooFinance();
var endDate = LocalDate.of(2017, 11, 29);
var dateRange = Range.of(endDate.minusYears(5), endDate);
var tickers = Array.of("VTI", "VEA", "VWO", "VTEB", "VIG", "XLE");
var returns = yahoo.getDailyReturns(dateRange, tickers);
var variance = returns.cols().stats().variance().colAt(0).toArray();
var volatility = variance.mapToDoubles(v -> Math.sqrt(252d * v.getDouble()));
var k = volatility.mapToDoubles(v -> 1d / v.getDouble()).stats().sum().doubleValue();
var riskParityWeights = volatility.mapToDoubles(v -> 1d / (v.getDouble() * k));
```

Next, consider a `DataFrame` of equal capital weights, equal risk weights and the Robo-Advisor proposed weights.

<?prettify?>
```java
var portfolios = DataFrame.of(tickers, String.class, columns -> {
    columns.add("Parity(Risk)", riskParityWeights);
    columns.add("Parity(Cap)", Range.of(0, 6).map(i -> 1d / 6d));
    columns.add("Robo(Proposed)", Array.of(0.35d, 0.21d, 0.16d, 0.15d, 0.08d, 0.05d));
});
```

<div class="frame"><pre class="frame">
     Index       |   VTI    |   VEA    |   VWO    |   BND    |   VIG    |   XLE    |
------------------------------------------------------------------------------------
   Parity(Risk)  |  12.41%  |  10.69%  |   8.39%  |  47.16%  |  13.68%  |   7.67%  |
    Parity(Cap)  |  16.67%  |  16.67%  |  16.67%  |  16.67%  |  16.67%  |  16.67%  |
 Robo(Proposed)  |  35.00%  |  21.00%  |  16.00%  |  15.00%  |   8.00%  |   5.00%  |
</pre></div>

The chart below illustrates where these three portfolios fall in the risk / return spectrum for this asset universe relative
to 100,000 randomly generated allocations. It appears that the equal **capital-weighted** portfolio has about the same risk as
the Robo-Advisor proposed portfolio, but significantly lower return. The **naive risk parity portfolio**, appears to exhibit
about half the risk of the other two candidates, but with lower return.

<p align="center">
    <img class="chart img-fluid" src="/images/morpheus/mpt/parity_1.png"/>
</p>

The code to generate this plot is as follows:

<?prettify?>
```java
var randomPortfolios = randomPortfolios(100000, tickers);
var randomRiskReturn = calcRiskReturn(randomPortfolios, endDate, false);
var caseStudies = calcRiskReturn(portfolios.transpose(), endDate, false);
var frames = portfolios.cols().keyArray().map(v -> {
    return caseStudies.rows().select(v.getValue()).cols().replaceKey("Return", v.getValue());
});

Chart.create().withScatterPlot(randomRiskReturn.cols().replaceKey("Return", "Random"), false, "Risk", chart -> {
    frames.forEachValue(v -> {
        chart.plot().<String>data().add(v.getValue(), "Risk");
        chart.plot().render(v.index() + 1).withDots(10);
    });
    chart.title().withText("Parity Portfolios vs Robo Advisor Allocation");
    chart.subtitle().withText("Investment Universe: VTI, VEA, VWO, VTEB, VIG, XLE");
    chart.plot().axes().domain().label().withText("Portfolio Risk");
    chart.plot().axes().domain().format().withPattern("0.00'%';-0.00'%'");
    chart.plot().axes().range(0).label().withText("Portfolio Return");
    chart.plot().axes().range(0).format().withPattern("0.00'%';-0.00'%'");
    chart.plot().style("Random").withColor(Color.LIGHT_GRAY);
    chart.legend().on().bottom();
    chart.show();
});
```
While the risk parity configuration exhibits a lower return, it is the **most efficient** of the three candidates as measured
by the [Sharpe Ratio](#sharpe-ratio). The table below summarizes these results, and shows that **over the past year**, our naive
risk parity portfolio realized a Sharpe of `3.17` versus `2.87` for the Robo-Advisor allocation.

<div class="frame"><pre class="frame">
     Index       |  Risk   |  Return  |  Sharpe  |
--------------------------------------------------
   Parity(Risk)  |  4.27%  |   8.36%  |    1.96  |
    Parity(Cap)  |  6.78%  |  12.42%  |    1.83  |
 Robo(Proposed)  |  6.71%  |  15.10%  |    2.25  |
</pre></div>

Making a judgement call on a single years performance is obviously not reliable, so consider the realized Sharpe ratios
of the 3 allocation strategies over the **past 10 years** for which returns are available for all the assets in this universe.
It appears from the chart below that the naive risk parity strategy out-performed the Robo-Advisor portfolio 7 out of the 10
years in risk-adjusted terms (assuming that the Robo-Advisor would suggest the same allocations each year, which may or may
not be true). The actual returns of the risk parity strategy are clearly going to be lower given that it will be overweight
low volatility / low return assets, however one could use **leverage** to boost the returns. It should be said however that
the use of **leverage is dangerous**, and something that is best left to a professional investment manager.

<p align="center">
    <img class="chart img-fluid" src="/images/morpheus/mpt/parity_2.png"/>
</p>

The code to generate this plot is as follows:

<?prettify?>
```java
var endDates = Range.ofLocalDates("2008-12-31", "2018-12-31", Period.ofYears(1));
var combined = DataFrame.concatColumns(endDates.map(end -> {
    var date = endDate.isAfter(end) ? end : endDate;
    var riskReturn = calcRiskReturn(portfolios.transpose(), date, true);
    return riskReturn.cols().select("Sharpe").cols().replaceKey("Sharpe", String.valueOf(date.getYear()));
}));

Chart.create().withBarPlot(combined.transpose(), false, chart -> {
    chart.title().withText("Realized Sharpe Ratios of Parity Portfolios vs Robo-Advisor");
    chart.subtitle().withText("Investment Universe: VTI, VEA, VWO, BND, VIG, XLE");
    chart.plot().axes().domain().label().withText("Realized Sharpe Ratio");
    chart.plot().axes().range(0).label().withText("Year");
    chart.legend().on().bottom();
    chart.show();
});
```

### 60/40 Stock-Bond

An often quoted benchmark is the **60/40 Stock-Bond portfolio**, which as the name implies, allocates 60% of capital to stocks
and 40% to bonds. This is an extremely **simple construction** as it involves only two assets and does not require any fancy
**mean-variance optimization** to be done. As a portfolio construction technique, it is the embodiment of the [KISS principle](https://en.wikipedia.org/wiki/KISS_principle).
In this section, we consider the relative efficiency of this portfolio to other potential weighting schemes, and also compare
it to the [Robo-Advisor](#robo-advisor) portfolio discussed in the previous section.

In order to simulate a 60/40 Stock-Bond portfolio, we are using two low cost ETFs as proxies, namely the **Vanguard Total
Stock Market ETF** with symbol [VTI](https://finance.yahoo.com/quote/VTI?p=VTI) and the **Vanguard Total Bond Market ETF**
with symbol [BND](https://finance.yahoo.com/quote/BND?p=BND). We can generate an infinite number of portfolios involving these
two assets, but 10,000 **fully invested long-only random combinations** should be more than sufficient to compare with our
60/40 allocation.

The plots illustrated below leverage the same code introduced in earlier sections of this article, but in this case involve
random portfolios investing in **VTI** and **BND** only. In addition, the past 1 year of asset returns to the end of September
2017 are used to compute portfolio risk and return.

The chart in the upper left quadrant shows the risk / return profile of our 10,000 random portfolios, which yields the
vaguely parabolic shape we have come to expect for a two asset portfolio. The blue square demonstrates where the **60/40 allocation**
falls on the [Efficient Frontier](https://en.wikipedia.org/wiki/Efficient_frontier), and looks to be reasonably well positioned.
The chart in the upper right quadrant shows the [Sharpe Ratio](#sharpe-ratio) decay when you sort the 10,000 random portfolios
from best to worst, and again, the **60/40 portfolio** holds up well (blue dot). It is also comforting to see that the **decay
in the Sharpe Ratio** for the top 6000 portfolios is very slow, suggesting that we need not be **overly precise** with our 60/40
allocation.

------

<div class="row">
    <div class="col-md-6">
        <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_7.png"/>
    </div>
    <div class="col-md-6">
        <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_8.png"/>
    </div>
</div>
<div class="row">
    <div class="col-md-6">
        <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_9.png"/>
    </div>
    <div class="col-md-6">
        <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_10.png"/>
    </div>
</div>

The chart in the lower left quadrant illustrates the equity curves of the 10,000 random portfolios, and demonstrates the wide
spread in outcomes over just 1 year. Our **60/40** portfolio fairs pretty well however, as shown by the plot in the lower right
quadrant. This chart highlights the **best** and **worst** portfolios out of the 10,000 candidates in terms of their respective
Sharpe Ratios. The 60/40 allocation **tracks the best portfolio** very closely and only slightly under performs in both **return
space** and **risk-adjusted space**. The table below documents the outcomes for these 3 portfolios.

<div class="frame"><pre class="frame">
 Index  |     Risk     |    Return     |    Sharpe    |
-------------------------------------------------------
 60/40  |  4.55079355  |  11.56909602  |  2.54221509  |
  Best  |   4.7632154  |  12.11536553  |   2.5435267  |
 Worst  |   3.2051807  |   0.13636278  |  0.04254449  |
</pre></div>

#### Embracing Simplicity

The **60/40 Stock-Bond allocation** portfolio appears to be a reasonable configuration, but how well does it compare to the
proposed Robo-Advisor portfolio discussed [earlier](#robo-advisor)? The scatter chart below illustrates the risk / return
profiles of the two configurations, and shows that their **Efficient Frontiers** based on asset returns for the past year
are not that dissimilar. Before jumping to any conclusions however, we should consider other investment horizons that are
perhaps less benign.

<p align="center">
    <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_11.png"/>
</p>

The code to generate the above plot is as follows.

<?prettify?>
```java
var count = 100000;
var endDate = LocalDate.of(2017, 9, 29);
//Generate risk / return profile for Stock-Bond universe
var tickers1 = Array.of("VTI", "BND");
var portfolios1 = randomPortfolios(count, tickers1);
var riskReturn1 = calcRiskReturn(portfolios1, endDate, false);
//Generate risk / return profile for Robo-Advisor universe
var tickers2 = Array.of("VTI", "VEA", "VWO", "VTEB", "VIG", "XLE");
var portfolios2 = randomPortfolios(count, tickers2);
var riskReturn2 = calcRiskReturn(portfolios2, endDate, false);
//Plot the results using a scatter plot
Chart.create().withScatterPlot(riskReturn2.cols().replaceKey("Return", "Robo"), false, "Risk", chart -> {
    chart.title().withText("Risk / Return Profiles: 60/40 Stock-Bond Versus Robo-Advisor");
    chart.subtitle().withText(count + " Portfolio Combinations Simulated");
    chart.plot().<String>data().add(riskReturn1.cols().replaceKey("Return", "60/40"), "Risk");
    chart.plot().render(1).withDots();
    chart.plot().axes().domain().label().withText("Portfolio Risk");
    chart.plot().axes().domain().format().withPattern("0.00'%';-0.00'%'");
    chart.plot().axes().range(0).label().withText("Portfolio Return");
    chart.plot().axes().range(0).format().withPattern("0.00'%';-0.00'%'");
    chart.legend().on().right();
    chart.show();
});
```

In order to avoid drawing the **wrong conclusion** from the above results, consider the same plots generated for
non-overlapping 1-year windows starting in 2008 until today. The plots below are generated using a minor modification of
the code just presented, and clearly show how the risk / return profiles of these configurations can vary over time. Note
that inorder to create these plots, the Robo-Advisor portfolio had to be modified slightly in that **BND** had to be
**substituted** for **VTEB** as the latter has a fairly recent inception date. This substitution is not likely to affect
these results in any material way.

----

<div class="row">
    <div class="col-md-6">
        <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_kiss_2008.png"/>
    </div>
    <div class="col-md-6">
        <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_kiss_2009.png"/>
    </div>
</div>
<div class="row">
    <div class="col-md-6">
        <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_kiss_2010.png"/>
    </div>
    <div class="col-md-6">
        <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_kiss_2011.png"/>
    </div>
</div>
<div class="row">
    <div class="col-md-6">
        <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_kiss_2012.png"/>
    </div>
    <div class="col-md-6">
        <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_kiss_2013.png"/>
    </div>
</div>
<div class="row">
    <div class="col-md-6">
        <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_kiss_2014.png"/>
    </div>
    <div class="col-md-6">
        <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_kiss_2015.png"/>
    </div>
</div>
<div class="row">
    <div class="col-md-6">
        <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_kiss_2016.png"/>
    </div>
    <div class="col-md-6">
        <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_kiss_2017.png"/>
    </div>
</div>

Note that the final plot labelled 2017 is a partial year and includes data to 12-Oct-2017.

### Reality Check

Most of the examples in this article have used the **past 1 year of asset returns** to Sep-2017 in order to compute portfolio
risk & return, and as a result, paint an **overly optimistic** picture of what one should expect from such portfolios. **2 Sharpe
portfolios** that generate well north of 10% returns in the current environment are truly outstanding, and not something to expect
over the long run. Some years will of course be better than others. To illustrate this, consider the **60/40 Stock-Bond portfolio**
to see how it has performed over **multiple non-overlapping 1-year periods** starting in 2008. The chart below tells the story.

<p align="center">
    <img class="chart img-fluid" src="/images/morpheus/mpt/mpt_12.png"/>
</p>

2008 was one of the **most brutal years** in recent financial history, where **systemic failure** was a genuine possibility at
times. The 60/40 Stock-Bond portfolio was **not spared** the anguish, and was down almost 30% at one point. The **average** of all
these equity curves, represented by the **bold black line** in the chart, generates a return of around `6.7%` at `3.7%` risk
implying a Sharpe Ratio of `1.8`. This is still an excellent outcome, but note how **deceiving averages** can be. None of the past
9 years looks **remotely average**, so beware of setting your expectations from averages when it comes to investing.

The code to generate the above plot is as follows:

<?prettify?>
```java
//Define DataFrame of position weights per 60/40 Stock-Bond allocation
var tickers = Array.of("VTI", "BND");
var portfolio = DataFrame.of(tickers, String.class, columns -> {
    columns.add("Weights", Array.of(0.6d, 0.4d));
});
//Generate 1-year equity curves starting in 2008 for 60/40 allocation
var dateRange = Range.of(LocalDate.of(2009, 1, 1), LocalDate.of(2018, 1, 1), Period.ofYears(1));
var frames = dateRange.map(date -> {
    var range = Range.of(date.minusYears(1), date);
    var equityCurve = getEquityCurves(range, portfolio.transpose());
    var start = equityCurve.rows().firstKey().get();
    return equityCurve
        .rows().mapKeys(DataFrameRow::ordinal)
        .cols().mapKeys(col -> String.valueOf(start.getYear()))
        .applyDoubles(v -> v.getDouble() * 100d);
});
//Combine equity curves into single frame and add mean of each curve
var combined = DataFrame.concatColumns(frames).rows().select(r -> !r.hasNulls());
//Add a column with the average of all equity curves
combined.cols().add("Mean", Double.class, v -> v.row().stats().mean());
//Plot the equity curves
Chart.create().withLinePlot(combined, chart -> {
    chart.title().withText("One-Year Equity Curves for 60/40 Stock/Bond Portfolio (Last 9 Years)");
    chart.subtitle().withText("Investment Universe: 60% VTI, 40% BND");
    chart.plot().axes().domain().label().withText("Calendar Days Since Start Of Year");
    chart.plot().axes().range(0).label().withText("Return");
    chart.plot().axes().range(0).format().withPattern("0.00'%';-0.00'%'");
    chart.plot().style("Mean").withColor(Color.BLACK).withLineWidth(1.5f);
    chart.legend().on().right();
    chart.show();
});
```


