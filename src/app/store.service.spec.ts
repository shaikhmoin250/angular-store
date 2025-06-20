import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { StoreService, CategoryDetails, ItemDetails } from './store.service'; // Assuming interfaces are exported

describe('StoreService', () => {
  let service: StoreService;
  let httpMock: HttpTestingController;
  let localStorageMock: any;

  // Mock data, ensure these match the structure your service expects/returns
  const mockCategoriesResponse: CategoryDetails[] = [
    { name: 'mens_outerwear', title: 'Mens Outerwear', image: 'assets/images/categories/mens_outerwear.jpg' },
    { name: 'ladies_outerwear', title: 'Ladies Outerwear', image: 'assets/images/categories/ladies_outerwear.jpg' },
    { name: 'mens_tshirts', title: 'Mens T-Shirts', image: 'assets/images/categories/mens_tshirts.jpg' },
    { name: 'ladies_tshirts', title: 'Ladies T-Shirts', image: 'assets/images/categories/ladies_tshirts.jpg' }
  ];

  const mockItemsMensOuterwear: ItemDetails[] = [
    { name: 'item1_mo', title: 'Mens Item 1', category: 'mens_outerwear', price: 100, description: 'Desc 1', image: 'img1.jpg', largeImage: 'large_img1.jpg' },
    { name: 'item2_mo', title: 'Mens Item 2', category: 'mens_outerwear', price: 120, description: 'Desc 2', image: 'img2.jpg', largeImage: 'large_img2.jpg' }
  ];

  beforeEach(() => {
    localStorageMock = {
      getItem: jasmine.createSpy('getItem').and.returnValue(null), // Default to empty cart
      setItem: jasmine.createSpy('setItem'),
      removeItem: jasmine.createSpy('removeItem'),
      clear: jasmine.createSpy('clear')
    };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      // StoreService is providedIn: 'root', so it's available without explicit providers array here
    });
    service = TestBed.inject(StoreService);
    httpMock = TestBed.inject(HttpTestingController);

    // Spy on localStorage globally and use our mock
    spyOn(localStorage, 'getItem').and.callFake(localStorageMock.getItem);
    spyOn(localStorage, 'setItem').and.callFake(localStorageMock.setItem);
    spyOn(localStorage, 'removeItem').and.callFake(localStorageMock.removeItem);
    spyOn(localStorage, 'clear').and.callFake(localStorageMock.clear);
  });

  afterEach(() => {
    httpMock.verify(); // Verify that no unmatched requests are outstanding
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getCategories', () => {
    it('should return an observable of hardcoded categories', (done) => {
      service.getCategories().subscribe(categories => {
        expect(categories.length).toBe(4); // Based on the hardcoded categories in service
        expect(categories[0].name).toEqual('mens_outerwear');
        expect(categories).toEqual(mockCategoriesResponse); // Compare with the full expected structure
        done();
      });
    });
  });

  describe('getItemsByCategory', () => {
    const categoryName = 'mens_outerwear';
    const mockUrl = `/assets/data/${categoryName}.json`;

    it('should fetch items for a category via HTTP GET', (done) => {
      service.getItemsByCategory(categoryName).subscribe(items => {
        expect(items).toEqual(mockItemsMensOuterwear);
        done();
      });

      const req = httpMock.expectOne(mockUrl);
      expect(req.request.method).toBe('GET');
      req.flush(mockItemsMensOuterwear); // Provide mock data as response
    });

    it('should cache items after first fetch', (done) => {
      // First call
      service.getItemsByCategory(categoryName).subscribe(items => {
        expect(items).toEqual(mockItemsMensOuterwear);
      });
      const req1 = httpMock.expectOne(mockUrl);
      req1.flush(mockItemsMensOuterwear);
      httpMock.verify(); // Verify first request is done

      // Second call - should use cache, no new HTTP request
      service.getItemsByCategory(categoryName).subscribe(items => {
        expect(items).toEqual(mockItemsMensOuterwear);
        done();
      });
      httpMock.expectNone(mockUrl); // Assert no new request was made
    });

    it('should return existing observable from cache if called multiple times concurrently before first fetch completes', (done) => {
      const obs1 = service.getItemsByCategory(categoryName);
      const obs2 = service.getItemsByCategory(categoryName);

      expect(obs1).toBe(obs2); // Should be the same observable instance due to caching logic

      let emissions = 0;
      const checkDone = () => {
        emissions++;
        if (emissions === 2) { // Ensure both subscriptions got the data
            done();
        }
      };

      obs1.subscribe(items => {
          expect(items).toEqual(mockItemsMensOuterwear);
          checkDone();
      });
      obs2.subscribe(items => { // This subscription should also receive the flushed data
          expect(items).toEqual(mockItemsMensOuterwear);
          checkDone();
      });

      const req = httpMock.expectOne(mockUrl); // Should only be one request
      req.flush(mockItemsMensOuterwear);
    });

  });

  describe('getItem', () => {
    const categoryName = 'mens_outerwear';
    // Using one of the item names from mockItemsMensOuterwear defined in the outer scope
    const existingItemName = 'item1_mo';
    const nonExistentItemName = 'non_existent_item';
    const mockUrl = `/assets/data/${categoryName}.json`; // Url used by getItemsByCategory

    it('should return an item by category and name when it exists', (done) => {
      service.getItem(categoryName, existingItemName).subscribe(item => {
        expect(item).toBeDefined();
        expect(item?.name).toBe(existingItemName);
        // Find the specific item from the mock array to compare
        const expectedItem = mockItemsMensOuterwear.find(i => i.name === existingItemName);
        expect(item).toEqual(expectedItem);
        done();
      });

      // Expect getItemsByCategory to be called internally by getItem, which then makes an HTTP request
      const req = httpMock.expectOne(mockUrl);
      expect(req.request.method).toBe('GET');
      req.flush(mockItemsMensOuterwear); // Flush the data for getItemsByCategory
    });

    it('should return undefined (as Observable) when item does not exist', (done) => {
      service.getItem(categoryName, nonExistentItemName).subscribe(item => {
        expect(item).toBeUndefined(); // The find method within getItem's map operator will return undefined
        done();
      });

      const req = httpMock.expectOne(mockUrl);
      req.flush(mockItemsMensOuterwear); // Provide data where the item is not present
    });

    it('should use cached category data if available for getItem', (done) => {
      // First, populate cache by calling getItemsByCategory
      service.getItemsByCategory(categoryName).subscribe();
      const req1 = httpMock.expectOne(mockUrl);
      req1.flush(mockItemsMensOuterwear);
      httpMock.verify(); // Ensure this request is handled and verified before proceeding

      // Now, call getItem - it should use the cached data from getItemsByCategory
      service.getItem(categoryName, existingItemName).subscribe(item => {
        expect(item).toBeDefined();
        expect(item?.name).toBe(existingItemName);
        done();
      });

      httpMock.expectNone(mockUrl); // Assert no new HTTP request was made by getItem for getItemsByCategory
    });
  });

  describe('Cart Functionality', () => {
    const mockItem1: ItemDetails = { name: 'item1', title: 'Item 1', category: 'cat1', price: 10, description: 'desc1', image: 'img1', largeImage: 'lrgImg1' };
    const mockItem2: ItemDetails = { name: 'item2', title: 'Item 2', category: 'cat2', price: 20, description: 'desc2', image: 'img2', largeImage: 'lrgImg2' };

    beforeEach(() => {
      // Reset cart and localStorage for each cart test
      // Accessing private member _cart for reset, or use a public reset method if available.
      // For testing, this direct manipulation or a test-specific method in service might be acceptable.
      // Let's assume service.setCart([]) is a public method or we rely on spies.
      // The service's internal cart array and cartSubject should be reset.
      // We also need to control what localStorage.getItem('cart') returns at the start of these tests.

      localStorageMock.getItem.calls.reset();
      localStorageMock.setItem.calls.reset();
      localStorageMock.removeItem.calls.reset(); // ensure all spies are reset
      localStorageMock.clear.calls.reset();

      // Ensure getCart() starts from a clean slate for these tests by controlling localStorage response.
      localStorageMock.getItem.and.returnValue(JSON.stringify([]));

      // And re-initialize the service's internal cart state by calling getCart() once,
      // which should load from the (mocked empty) localStorage.
      // This is to ensure the service's internal cart array is also empty.
      // A more direct way would be to call a (hypothetical) service.resetCart() method.
      // For now, we'll rely on the next getCart() call in tests to initialize from the mocked localStorage.
      // Or, if StoreService had a public way to clear/reset its cart state:
      // service.clearCart(); // if clearCart also clears in-memory and subject
      // Forcing re-initialization of the cartSubject if it was more complex:
      // if (service['cartSubject']) { service['cartSubject'].complete(); service['cartSubject'] = new BehaviorSubject([]); }
      // The `setCart` method in the actual service already updates the subject.
      // So, to clear the in-memory cart for testing, we can call it.
      // service.setCart([]); // This was not a method in the original service, let's remove this line
                           // and rely on localStorageMock for initial state.
    });

    describe('getCart', () => {
      it('should retrieve cart from localStorage on first call if cartSubject is not initialized or empty', (done) => {
        const storedCart = [{ item: mockItem1, size: 'M', quantity: 1 }];
        localStorageMock.getItem.and.returnValue(JSON.stringify(storedCart));

        // To ensure cartSubject is not pre-populated from other test suites, we might need a fresh service instance
        // or a way to reset its internal state. TestBed.inject(StoreService) gives a fresh one for the top describe,
        // but this nested describe needs to be careful.
        // For this test, we assume getCart will read from localStorage if its internal cart is empty or uninitialized.
        // The service logic is: if (this._cart.length === 0 && localStorage.getItem('cart'))

        service.getCart().subscribe(cart => {
          expect(localStorageMock.getItem).toHaveBeenCalledWith('cart');
          expect(cart).toEqual(storedCart);
          done();
        });
      });

      it('should return existing cart observable (and its current value) if already initialized', (done) => {
        // Initialize cart with some data through addItemToCart, which populates the subject
        service.addItemToCart(mockItem1, 'S', 1);
        localStorageMock.getItem.calls.reset(); // Reset spy after initial setup calls

        service.getCart().subscribe(cart => {
          expect(localStorageMock.getItem).not.toHaveBeenCalled();
          expect(cart.length).toBe(1);
          expect(cart[0].item).toEqual(mockItem1);
          done();
        });
      });
    });

    describe('addItemToCart', () => {
      it('should add a new item to the cart and update localStorage', (done) => {
        service.addItemToCart(mockItem1, 'M', 1);
        service.getCart().subscribe(cart => {
          if (cart.length === 1) { // Wait for the cart to update
            expect(cart[0].item).toEqual(mockItem1);
            expect(cart[0].quantity).toBe(1);
            expect(cart[0].size).toBe('M');
            expect(localStorageMock.setItem).toHaveBeenCalledWith('cart', JSON.stringify(cart));
            done();
          }
        });
      });

      it('should update quantity if item with same name and size already exists', (done) => {
        service.addItemToCart(mockItem1, 'M', 1); // First add
        service.addItemToCart(mockItem1, 'M', 2); // Add same item and size

        service.getCart().subscribe(cart => {
          // This subscription might fire multiple times as items are added.
          // We are interested in the final state after the second add.
          if (cart.length === 1 && cart[0].quantity === 3) {
            expect(localStorageMock.setItem).toHaveBeenCalledTimes(2); // Called after each addition
            done();
          }
        });
      });

      it('should add as new cart entry if item exists but size is different', (done) => {
        service.addItemToCart(mockItem1, 'M', 1);
        service.addItemToCart(mockItem1, 'L', 1);

        service.getCart().subscribe(cart => {
          if (cart.length === 2) { // Wait for the cart to update with the second distinct item
            expect(localStorageMock.setItem).toHaveBeenCalledTimes(2);
            done();
          }
        });
      });
    });

    describe('removeCartItem', () => {
      it('should remove an item from the cart and update localStorage', (done) => {
        const cartItem1 = { item: mockItem1, size: 'M', quantity: 1 };
        const cartItem2 = { item: mockItem2, size: 'S', quantity: 2 };
        service.addItemToCart(mockItem1, 'M', 1);
        service.addItemToCart(mockItem2, 'S', 2);

        // Wait for additions to complete before removal
        let addCount = 0;
        const tempSub = service.getCart().subscribe(currentCart => {
            addCount++;
            if (addCount === 2 && currentCart.length === 2) { // Both items added
                tempSub.unsubscribe(); // Stop listening to intermediate states

                service.removeCartItem(cartItem1); // Remove mockItem1

                service.getCart().subscribe(finalCart => {
                    expect(finalCart.length).toBe(1);
                    expect(finalCart[0].item).toEqual(mockItem2);
                    // setItem calls: 2 for add, 1 for remove
                    expect(localStorageMock.setItem).toHaveBeenCalledTimes(3);
                    done();
                });
            }
        });
      });

      it('should do nothing if item to remove is not found', (done) => {
        service.addItemToCart(mockItem1, 'M', 1);
        const nonExistentCartItem = { item: mockItem2, size: 'L', quantity: 1 };

        let initialAddDone = false;
        const tempSub = service.getCart().subscribe(currentCart => {
            if (currentCart.length === 1 && !initialAddDone) {
                initialAddDone = true;
                tempSub.unsubscribe();

                service.removeCartItem(nonExistentCartItem);

                service.getCart().subscribe(finalCart => {
                    expect(finalCart.length).toBe(1);
                     // setItem calls: 1 for add, 1 for remove (even if no change, service calls saveCart)
                    expect(localStorageMock.setItem).toHaveBeenCalledTimes(2);
                    done();
                });
            }
        });
      });
    });

    describe('updateCartItemQuantity', () => {
      it('should update the quantity of an existing item in cart and update localStorage', (done) => {
        const cartItemToUpdate = { item: mockItem1, size: 'M', quantity: 1 };
        service.addItemToCart(mockItem1, 'M', 1);

        let initialAddDone = false;
        const tempSub = service.getCart().subscribe(currentCart => {
            if (currentCart.length === 1 && !initialAddDone) {
                initialAddDone = true;
                tempSub.unsubscribe();

                service.updateCartItemQuantity({ ...cartItemToUpdate, quantity: 5 });

                service.getCart().subscribe(finalCart => {
                    if (finalCart.length === 1 && finalCart[0].quantity === 5) {
                        expect(localStorageMock.setItem).toHaveBeenCalledTimes(2); // 1 for add, 1 for update
                        done();
                    }
                });
            }
        });
      });

      it('should add the item if it does not exist in cart (service logic) and update localStorage', (done) => {
        const newItemForCart = { item: mockItem1, size: 'M', quantity: 3 };
        service.updateCartItemQuantity(newItemForCart); // Item not in cart yet

        service.getCart().subscribe(cart => {
          if (cart.length === 1 && cart[0].quantity === 3) { // Item added
            expect(cart[0].item).toEqual(mockItem1);
            expect(localStorageMock.setItem).toHaveBeenCalledWith('cart', JSON.stringify(cart));
            expect(localStorageMock.setItem).toHaveBeenCalledTimes(1); // Only 1 call for update (which acts as add)
            done();
          }
        });
      });
    });
  });

  describe('processOrder', () => {
    const mockItem1: ItemDetails = { name: 'item1', title: 'Item 1', category: 'cat1', price: 10, description: 'desc1', image: 'img1', largeImage: 'lrgImg1' };
    const mockCart: CartItemDetails[] = [
      { item: mockItem1, size: 'M', quantity: 1 }
    ];
    const mockPaymentData = {
      shippingAddress: '123 Test St',
      shippingOptionData: { id: 'standard' }, // Ensure this matches the structure used in service
      paymentMethodData: { type: 'card' }     // Ensure this matches the structure used in service
    };

    it('should return a Promise that resolves with an orderId object containing a string orderId', async () => {
      // Using async/await for cleaner Promise testing
      const result = await service.processOrder(mockCart, mockPaymentData);
      expect(result).toBeDefined();
      expect(result.orderId).toBeDefined();
      expect(typeof result.orderId).toBe('string'); // As it's based on Date.now().valueOf().toString()
    });

    it('should log order details to the console', async () => {
      spyOn(console, 'log'); // Spy on console.log

      await service.processOrder(mockCart, mockPaymentData);

      // Verify the console.log was called with the expected arguments
      // The actual service logs shippingOptionData.id, not the whole object.
      expect(console.log).toHaveBeenCalledWith(
        'TODO: send order to server',
        mockCart,
        mockPaymentData.shippingAddress,
        mockPaymentData.shippingOptionData.id, // Service logs shippingOptionData.id
        mockPaymentData.paymentMethodData
      );
    });
  });
});
