__author__ = 'oshri'

"""		"		"PriceUpdateDate"             : "2015-02-01 10:07:00", 			// @timestamp(yyyy-mm-dd hh:mm:ss) - price update timestamp, if your store have two diffrent fields(date and time), convert to this field
					"ItemCode"                    : "1000801", 								// @string - product id/code
					"ItemType"                    : "1", 											// @boolean - product type - weight/no weight
					"ItemName"                    : "òåâú ÷øðõ ôøâ 500 âø", 				// @string - product name
					"ManufacturerName"            : "", 										// @string - manufacture name
					"ManufactureCountry"          : "éùøàì", 								// @string - manufacture country from list of countries, of your country not inlude in this list pars it
					"ManufacturerItemDescription" : "òåâú ÷øðõ ôøâ 500 âø", 		// @string - product description
					"SoldByWeight"                : "", 											// @bool - is the item sold by weight
					"Quantity"                    : "1000.00", 									// @float - quantity
					"UnitOfMeasure"               : "÷â", 										// @enum of units of measure (÷â, ìéèø, âøí, îéìéèø, åëå)
					"QtyInPackage"                : "0", 											// @int - item quantity
					"UnitOfMeasurePrice"          : "0.0187", 								// @float - measure unit for item
					"AllowDiscount"               : "1", 											// @boolean - allow discount
					"bIsWeighted": "1", 															//@bool 1/0 -unknown
					"ItemPrice": "3.90", 															//@float the price of the item
					"UnitOfMeasurePrice"          : "0.0187", 								// @float - measure unit for item
					"AllowDiscount"               : "1", 											// @boolean - allow discount
					"ItemStatus"                  : "1" 												// @int (maybe boolean) - unknown"             : "2015-02-01 10:07:00", 			// @timestamp(yyyy-mm-dd hh:mm:ss) - price update timestamp, if your store have two diffrent fields(date and time), convert to this field
					"ItemCode"                    : "1000801", 								// @string - product id/code
					"ItemType"                    : "1", 											// @boolean - product type - weight/no weight
					"ItemName"                    : "òåâú ÷øðõ ôøâ 500 âø", 				// @string - product name
					"ManufacturerName"            : "", 										// @string - manufacture name
					"ManufactureCountry"          : "éùøàì", 								// @string - manufacture country from list of countries, of your country not inlude in this list pars it
					"ManufacturerItemDescription" : "òåâú ÷øðõ ôøâ 500 âø", 		// @string - product description
					"SoldByWeight"                : "", 											// @bool - is the item sold by weight
					"Quantity"                    : "1000.00", 									// @float - quantity
					"UnitOfMeasure"               : "÷â", 										// @enum of units of measure (÷â, ìéèø, âøí, îéìéèø, åëå)
					"QtyInPackage"                : "0", 											// @int - item quantity
					"UnitOfMeasurePrice"          : "0.0187", 								// @float - measure unit for item
					"AllowDiscount"               : "1", 											// @boolean - allow discount
					"bIsWeighted": "1", 															//@bool 1/0 -unknown
					"ItemPrice": "3.90", 															//@float the price of the item
					"UnitOfMeasurePrice"          : "0.0187", 								// @float - measure unit for item
					"AllowDiscount"               : "1", 											// @boolean - allow discount
					"ItemStatus"                  : "1" 												// @int (maybe boolean) - unknown
"""
class root():

    def __int__(self,DllVerNo,SubChainId,StoreId,BikoretNo):
        self.DllverNo = DllVerNo;
        self.SubChainId = SubChainId;
        self.StoreId = StoreId;
        self.BikoretnNo = BikoretNo;
        self.Items = [];


class Item():

    def __init__(self,PriceUpdateDate,ItemCode,ItemType,ItemName,ManufacturerName,ManufactureCountry,
                 ManufacturerItemDescription,SoldByWeight,Quantity,UnitOfMeasure,QtyInPackage,UnitOfMeasurePrice,
                 AllowDiscount,bIsWeighted,ItemPrice,ItemStatus,):




