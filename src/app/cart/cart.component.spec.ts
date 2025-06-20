import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { BehaviorSubject, of } from 'rxjs'; // Import of for StoreService mock
import { CartComponent } from './cart.component';
import { StoreService, CartItemDetails, ItemDetails } from '../store.service'; // Import necessary interfaces

// Helper to create mock ItemDetails
const createMockItem = (id: string, price: number): ItemDetails => ({
  name: id,
  title: `Item ${id}`,
  category: 'cat', // Default category
  price: price,
  description: `Description ${id}`,
  image: `img${id}.jpg`,
  largeImage: `lrgImg${id}.jpg`
});

describe('CartComponent', () => {
  let component: CartComponent;
  let fixture: ComponentFixture<CartComponent>;
  let mockStoreService: jasmine.SpyObj<StoreService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let cartSubject: BehaviorSubject<CartItemDetails[]>;

  const item1: ItemDetails = createMockItem('item1', 10);
  const item2: ItemDetails = createMockItem('item2', 25);

  beforeEach(async () => {
    cartSubject = new BehaviorSubject<CartItemDetails[]>([]); // Initialize with empty cart

    mockStoreService = jasmine.createSpyObj('StoreService', [
      'getCart',
      'removeCartItem',
      'updateCartItemQuantity',
      'processOrder',
      'setCart' // Assuming setCart might be used internally or for testing setup
    ]);
    mockStoreService.getCart.and.returnValue(cartSubject.asObservable());
    // Mock processOrder to return a resolved promise with a dummy orderId
    mockStoreService.processOrder.and.resolveTo({ orderId: 'mockOrderId' });


    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [CartComponent],
      providers: [
        { provide: StoreService, useValue: mockStoreService },
        { provide: Router, useValue: mockRouter }
      ]
      // No need to import modules like HttpClientModule here as service is mocked
      // If CartComponent used Angular Material components, those modules would be needed
    }).compileComponents();

    fixture = TestBed.createComponent(CartComponent);
    component = fixture.componentInstance;
    // fixture.detectChanges(); // Delay initial detectChanges for more granular ngOnInit testing
  });

  it('should create', () => {
    fixture.detectChanges(); // Initial data binding and ngOnInit call
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should subscribe to getCart and update local cart and payment request total', () => {
      const initialCart: CartItemDetails[] = [
        { item: item1, size: 'M', quantity: 2 }, // 2 * 10 = 20
        { item: item2, size: 'L', quantity: 1 }  // 1 * 25 = 25
      ];
      cartSubject.next(initialCart); // Emit initial cart state before ngOnInit

      fixture.detectChanges(); // Trigger ngOnInit

      expect(mockStoreService.getCart).toHaveBeenCalled();
      expect(component.cart).toEqual(initialCart);
      // Check if paymentRequest is defined before accessing its properties
      expect(component.paymentRequest).toBeDefined();
      if (component.paymentRequest) {
        expect(component.paymentRequest.transactionInfo.totalPrice).toBe('45.00'); // 20 + 25
      }
    });

    it('should update payment request total when cart changes after ngOnInit', () => {
      fixture.detectChanges(); // ngOnInit called, cart is initially empty, total is "0.00"

      const newCart: CartItemDetails[] = [{ item: item1, size: 'S', quantity: 3 }]; // 3 * 10 = 30
      cartSubject.next(newCart);
      fixture.detectChanges(); // Reflect cart update

      expect(component.cart).toEqual(newCart);
      expect(component.paymentRequest).toBeDefined();
      if (component.paymentRequest) {
        expect(component.paymentRequest.transactionInfo.totalPrice).toBe('30.00');
      }
    });
  });

  describe('Computed Properties', () => {
    beforeEach(() => {
      // For these tests, directly set component.cart.
      // No need to call detectChanges unless template interaction is tested (not here).
    });

    it('cartSize should calculate total quantity of items', () => {
      component.cart = [
        { item: item1, size: 'M', quantity: 2 },
        { item: item2, size: 'L', quantity: 3 }
      ];
      expect(component.cartSize).toBe(5);

      component.cart = [];
      expect(component.cartSize).toBe(0);

      component.cart = [ // Test with single item
        { item: item1, size: 'M', quantity: 10 }
      ];
      expect(component.cartSize).toBe(10);
    });

    it('cartTotal should calculate total price of items', () => {
      component.cart = [
        { item: item1, size: 'M', quantity: 2 }, // 2 * 10 = 20
        { item: item2, size: 'L', quantity: 1 }  // 1 * 25 = 25
      ];
      expect(component.cartTotal).toBe(45);

      component.cart = [];
      expect(component.cartTotal).toBe(0);

      component.cart = [ // Test with single item, multiple quantity
        { item: item1, size: 'M', quantity: 3 } // 3 * 10 = 30
      ];
      expect(component.cartTotal).toBe(30);
    });
  });

  describe('User Actions', () => {
    // item1 is from the outer scope, already defined.
    const cartItem1Data: CartItemDetails = { item: item1, size: 'M', quantity: 2 };

    beforeEach(() => {
      // Initialize component's cart for these action tests
      // This ensures that operations like onRemove have an item to act upon.
      cartSubject.next([cartItem1Data]); // Emit to simulate cart being populated via ngOnInit subscription
      fixture.detectChanges(); // Reflect this initial cart state

      // Reset spies for specific User Actions tests
      mockStoreService.removeCartItem.calls.reset();
      mockStoreService.updateCartItemQuantity.calls.reset();
      mockRouter.navigate.calls.reset();
    });

    it('onCheckout should navigate to /checkout', () => {
      component.onCheckout();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/checkout']);
    });

    it('onRemove should call storeService.removeCartItem with the correct item', () => {
      // component.cart should already have cartItem1Data due to beforeEach setup
      expect(component.cart.length).toBe(1); // Ensure cart is populated as expected
      const itemToRemove = component.cart[0]; // Get the actual item instance from component's cart

      component.onRemove(itemToRemove);
      expect(mockStoreService.removeCartItem).toHaveBeenCalledWith(itemToRemove);
    });

    describe('onQuantityChange', () => {
      let mockEvent: Partial<Event>;
      let mockInputElement: Partial<HTMLInputElement>;
      const originalCartItem: CartItemDetails = { item: item1, size: 'S', quantity: 1 }; // A fresh item for these tests

      beforeEach(() => {
        mockInputElement = {
          valueAsNumber: 5 // Default new quantity
        };
        mockEvent = {
          target: mockInputElement as HTMLInputElement
        };
        // Reset cart to only this item for onQuantityChange tests to avoid interference
        cartSubject.next([originalCartItem]);
        fixture.detectChanges();
      });

      it('should call storeService.updateCartItemQuantity with updated item details', () => {
        component.onQuantityChange(mockEvent as Event, originalCartItem);

        expect(mockStoreService.updateCartItemQuantity).toHaveBeenCalledWith({
          ...originalCartItem,
          quantity: 5
        });
      });

      it('should call storeService.updateCartItemQuantity with NaN if input valueAsNumber is NaN', () => {
        // This test verifies that the component passes through NaN if the input element provides it.
        // The service would then be responsible for handling or ignoring NaN.
        mockInputElement.valueAsNumber = NaN;
        component.onQuantityChange(mockEvent as Event, originalCartItem);

        expect(mockStoreService.updateCartItemQuantity).toHaveBeenCalledWith({
          ...originalCartItem,
          quantity: NaN
        });
      });

      it('should not call updateCartItemQuantity if event target is null', () => {
        mockEvent.target = null; // Simulate a scenario where target is null
        component.onQuantityChange(mockEvent as Event, originalCartItem);
        expect(mockStoreService.updateCartItemQuantity).not.toHaveBeenCalled();
      });

      it('should not call updateCartItemQuantity if event target is not an HTMLInputElement', () => {
        // Simulate a different type of target (though less likely for this specific event binding)
        mockEvent.target = {} as HTMLInputElement;
        component.onQuantityChange(mockEvent as Event, originalCartItem);
        expect(mockStoreService.updateCartItemQuantity).not.toHaveBeenCalled();
      });
    });
  });

  // google.payments.api.PaymentData is a global type from Google Pay SDK script
  // For testing, we might need to declare it or use a simplified mock interface if not available globally
  // For this environment, we assume the type might not be fully available and use `any` or a simplified mock.
  // Let's define a simplified interface for the mock if google types are not present.
  interface MockGooglePaymentData {
    apiVersion: number;
    apiVersionMinor: number;
    paymentMethodData: {
      description?: string;
      tokenizationData: {
        type: string;
        token: string;
      };
      type: string;
      info?: {
        cardNetwork: string;
        cardDetails: string;
      };
    };
  }


  describe('Google Pay Integration - onLoadPaymentData', () => {
    let mockPaymentData: MockGooglePaymentData;
    let mockCustomEvent: CustomEvent<MockGooglePaymentData>;

    beforeEach(() => {
      // Reset cart and spies
      cartSubject.next([{ item: item1, size: 'M', quantity: 1 }]); // item1 from outer scope
      fixture.detectChanges(); // Reflect cart changes

      mockStoreService.processOrder.calls.reset();
      mockStoreService.setCart.calls.reset(); // setCart is part of StoreService spy, used in component
      mockRouter.navigate.calls.reset();

      mockPaymentData = {
        apiVersion: 2,
        apiVersionMinor: 0,
        paymentMethodData: {
          description: 'Visa •••• 1234',
          tokenizationData: {
            type: 'PAYMENT_GATEWAY',
            token: 'examplePaymentMethodToken'
          },
          type: 'CARD',
          info: {
            cardNetwork: 'VISA',
            cardDetails: '1234'
          }
        }
      };
      // Ensure CustomEvent can be constructed. If not globally available, this might need adjustment.
      // In a real browser environment, CustomEvent is standard. In Node for Jest/Jasmine, it might need polyfill/mock.
      // For Angular/Karma, it's usually available.
      mockCustomEvent = new CustomEvent<MockGooglePaymentData>('loadpaymentdata', { // Using CustomEvent
        detail: mockPaymentData
      });

      // Ensure processOrder returns a resolved promise for these tests by default
      mockStoreService.processOrder.and.resolveTo({ orderId: 'mockOrderId123' });
    });

    it('should call storeService.processOrder with cart and paymentData detail', async () => {
      await component.onLoadPaymentData(mockCustomEvent as Event); // Cast CustomEvent to Event
      expect(mockStoreService.processOrder).toHaveBeenCalledWith(component.cart, mockPaymentData);
    });

    it('should call storeService.setCart with an empty array after successful order', async () => {
      await component.onLoadPaymentData(mockCustomEvent as Event);
      // Ensure processOrder promise has resolved before checking subsequent calls
      // await fixture.whenStable(); // Ensure all microtasks/promises are done
      expect(mockStoreService.processOrder).toHaveBeenCalled(); // Prerequisite
      expect(mockStoreService.setCart).toHaveBeenCalledWith([]);
    });

    it('should navigate to /confirm after successful order', async () => {
      await component.onLoadPaymentData(mockCustomEvent as Event);
      // await fixture.whenStable();
      expect(mockStoreService.processOrder).toHaveBeenCalled(); // Prerequisite
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/confirm']);
    });

    it('should not clear cart or navigate if processOrder fails', async () => {
      const errorMsg = 'Order processing failed';
      mockStoreService.processOrder.and.rejectWith(new Error(errorMsg));

      try {
        await component.onLoadPaymentData(mockCustomEvent as Event);
      } catch (e:any) { // Check for any error being thrown
        // Expecting the error from processOrder to be caught by async/await
        expect(e.message).toBe(errorMsg);
      }
      // Ensure that after a failure, cart is not cleared and navigation does not occur
      expect(mockStoreService.setCart).not.toHaveBeenCalled();
      expect(mockRouter.navigate).not.toHaveBeenCalledWith(['/confirm']);
    });

    it('should call processOrder, then setCart, then navigate in sequence on success', async () => {
      // Use a controllable promise for processOrder
      let processOrderResolver!: (value: { orderId: string }) => void;
      const processOrderPromise = new Promise<{ orderId: string }>(resolve => {
        processOrderResolver = resolve;
      });
      mockStoreService.processOrder.and.returnValue(processOrderPromise);

      // Call the method, it will await the processOrderPromise
      component.onLoadPaymentData(mockCustomEvent as Event);

      // At this point, processOrder is called but hasn't resolved yet
      expect(mockStoreService.processOrder).toHaveBeenCalled();
      expect(mockStoreService.setCart).not.toHaveBeenCalled();
      expect(mockRouter.navigate).not.toHaveBeenCalled();

      // Resolve the processOrder promise
      processOrderResolver({ orderId: 'testOrderIdSequence' });

      // Wait for the promise chain in onLoadPaymentData to complete
      await fixture.whenStable(); // This waits for async operations in the component to complete

      expect(mockStoreService.setCart).toHaveBeenCalledWith([]);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/confirm']);
      // Verify order of calls if necessary using jasmine-ordered-spies or similar,
      // or by checking calls.all().length and order if spies store all calls.
      // For now, this sequence check is implicit by awaiting.
    });
  });
});
