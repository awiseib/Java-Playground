# IBKR Java WebAPI Samples

## Author
[Andrew Wise / awiseib](https://github.com/awiseib)

## Purpose

The content included here demonstrates Java implementations for the TWS API. 
This should not be used as an example of a perfect trading system, but a means of implementing the TWS API with standard Java libraries. 

It is important to note there are numerous ways to code. This particular example makes reference to the EWrapperImpl.java. The Impl file was created as a means to store all EWrapper responses. This would be an exceptional method to maintain a more condensed request file as opposed to building the full wrapper logic every time.

The method used for all other code is built with the intent to be self contained beyond what was used from the IBApi files directly.

## Build Details

### Java Packages
All Java packeges in use here are intended to use the core Java library without the need for external systems. These can be implemented using the default JDK packages.

## Note about TWS API Implementation
For users looking to use utilize the TWS API, please be aware you must have an IBKR PRO account that is opened and funded. For users looking to retrieve market data, users must have a minimum of $500 USD or your currency's equivalent. 

## Contributing

Pull requests are welcome. However, please be aware that these files are intended strictly for
demonstration purposes, and so only requests that would be globally beneficial will be approved.

## License

[MIT](https://choosealicense.com/licenses/mit/)

