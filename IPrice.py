__author__ = 'oshri'

"""
		"PriceUpdateDate"             : "2015-02-01 10:07:00", 			// @timestamp(yyyy-mm-dd hh:mm:ss) - price update timestamp, if your store have two diffrent fields(date and time), convert to this field
					"ItemCode"                    : "1000801", 								// @string - product id/code
					"ItemType"                    : "1", 											// @boolean - product type - weight/no weight
					"ItemName"                    : "���� ���� ��� 500 ��", 				// @string - product name
					"ManufacturerName"            : "", 										// @string - manufacture name
					"ManufactureCountry"          : "�����", 								// @string - manufacture country from list of countries, of your country not inlude in this list pars it
					"ManufacturerItemDescription" : "���� ���� ��� 500 ��", 		// @string - product description
					"SoldByWeight"                : "", 											// @bool - is the item sold by weight
					"Quantity"                    : "1000.00", 									// @float - quantity
					"UnitOfMeasure"               : "��", 										// @enum of units of measure (��, ����, ���, ������, ���)
					"QtyInPackage"                : "0", 											// @int - item quantity
					"UnitOfMeasurePrice"          : "0.0187", 								// @float - measure unit for item
					"AllowDiscount"               : "1", 											// @boolean - allow discount
					"bIsWeighted": "1", 															//@bool 1/0 -unknown
					"ItemPrice": "3.90", 															//@float the price of the item
					"UnitOfMeasurePrice"          : "0.0187", 								// @float - measure unit for item
					"AllowDiscount"               : "1", 											// @boolean - allow discount
					"ItemStatus"                  : "1" 												// @int (maybe boolean) - unknown
"""
class root(object):

	def __int__(self,DllVerNo,SubChainId,StoreId,BikoretNo):
		self.DllverNo = DllVerNo
		self.SubChainId = SubChainId
		self.StoreId = StoreId
		self.BikoretnNo = BikoretNo
		self.Items = []

class Item(object):

	def __init__(self,PriceUpdateDate,ItemCode, ItemType ,ItemName,ManufactureName,ManufactureCountry,
                 ManufactureItemDescription,SoldByWeight,Quantity,UnitOfMeasure,QtyInPackage,UnitOfMeasurePrice,
                 AllowDiscount,bIsWeighted,ItemPrice,ItemStatus):
		self.PriceUpdateDate = PriceUpdateDate  	# @timestamp(yyyy-mm-dd hh:mm:ss) - price update timestamp
		self.ItemCode = ItemCode 					# @string - product id/code
		self.ItemType = ItemType 					# @boolean - product type - weight/no weight
		self.ItemName = ItemName  					# @string - product name
		self.ManufactureName = ManufactureName 		# @string - manufacture name
		self.ManufactureCountry = ManufactureCountry 	# @string - manufacture country from list of countries, of your country not inlude in this list pars it
		self.ManufactureItemDescription = ManufactureItemDescription #@string - product description
		self.SoldByWeight = SoldByWeight 			# @bool - is the item sold by weight
		self.Quantity = Quantity 					# @float - quantity
		self.UnitOfMeasure = UnitOfMeasure 			# @enum of units of measure
		self.QtyInPackage = QtyInPackage  			# @int - item quantity
		self.UnitOfMeasurePrice = UnitOfMeasurePrice # @float - measure unit for item
		self.AllowDiscount = AllowDiscount 			# @boolean - allow discount
		self.bIsWeighted = bIsWeighted 				# @bool 1/0 -unknown
		self.ItemPrice = ItemPrice  				# @float the price of the item
		self.ItemStatus = ItemStatus 				# @int (maybe boolean) - unknown





