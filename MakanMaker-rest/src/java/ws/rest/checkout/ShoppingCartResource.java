/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws.rest.checkout;

import ejb.session.stateless.CustomerControllerLocal;
import ejb.session.stateless.OrderControllerLocal;
import ejb.session.stateless.ShoppingCartControllerLocal;
import entity.MealKitEntity;
import entity.OrderEntity;
import entity.ShoppingCartEntity;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import rest.datamodel.customer.CreateOrdersRequest;
import rest.datamodel.customer.MsgResponse;
import rest.datamodel.customer.ShoppingCartResponse;
import util.helperClass.CartItemWrapper;

/**
 * REST Web Service
 *
 * @author yingshi
 */
@Path("shoppingCart")
public class ShoppingCartResource {

    OrderControllerLocal orderController = lookupOrderControllerLocal();

    ShoppingCartControllerLocal shoppingCartController = lookupShoppingCartControllerLocal();

    CustomerControllerLocal customerController = lookupCustomerControllerLocal();

    
    
    
    @Context
    private UriInfo context;

    /**
     * Creates a new instance of ShoppingCartResource
     */
    public ShoppingCartResource() {
    }

    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveShoppingCart(@QueryParam("customerId") String customerId){
        try{
            Long id = Long.parseLong(customerId);
            
            ShoppingCartEntity cart = customerController.retrieveShoppingCartByCustomerId(id,true);
            
            List<CartItemWrapper> items = new ArrayList<>();
            int i=0;
            List<MealKitEntity> mealKits = shoppingCartController.retrieveMealKitsByCustomerId(cart.getShoppingCartId(),true);
            List<Integer> qs = cart.getQuantity();
            double subtotal = shoppingCartController.calculatePriceByCartId(cart.getShoppingCartId());
            for (MealKitEntity m : mealKits) {
                m.setRecipe(null);
                m.setIngredients(null);
                
                items.add(new CartItemWrapper(qs.get(i), m));
                i++;
            }
            
            ShoppingCartResponse rsp = new ShoppingCartResponse(items, subtotal, true, "Retrieve Success");
            
            return Response.status(Response.Status.OK).entity(rsp).build();
        }catch(NumberFormatException ex){
            ShoppingCartResponse rsp = new ShoppingCartResponse(null, 0, false, "Query Param Formatting Wrongly."+ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(rsp).build();
        }
        catch(Exception ex){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        
    }
    
    @Path("delete")
    @DELETE
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteItem(@QueryParam("customerId") String customerId, @QueryParam("mealKitId") String mealKitId){
        MsgResponse rsp;
        try{
            shoppingCartController.deleteIten(Long.parseLong(customerId), Long.parseLong(mealKitId));
            rsp = new MsgResponse("Removed!", true);
            return Response.status(Response.Status.OK).entity(rsp).build();
        }catch(NumberFormatException ex){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }catch(Exception ex){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Path("add")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addItem(@QueryParam("customerId") String customerId, @QueryParam("mealKitId") String mealKitId,@QueryParam("qty") String qty){
        MsgResponse rsp;
        try{
            Long cartId = customerController.retrieveShoppingCartByCustomerId(Long.parseLong(customerId), true).getShoppingCartId();
            shoppingCartController.addItem(Long.parseLong(mealKitId),Integer.parseInt(qty),cartId);
            rsp = new MsgResponse("Add!", true);
            return Response.status(Response.Status.OK).entity(rsp).build();
        }catch(NumberFormatException ex){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }catch(Exception ex){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Path("create")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOrder(JAXBElement<CreateOrdersRequest> jaxbCreateOrderReq) {
        MsgResponse rsp;
        
         if ((jaxbCreateOrderReq != null) && (jaxbCreateOrderReq.getValue() != null)) {
            try {
                CreateOrdersRequest req = jaxbCreateOrderReq.getValue();
                int i=0;
                for (OrderEntity o: req.getOrders()){
                    orderController.createNewOrder(o, req.getCustomerId(), req.getMealKitIds().get(i), req.getAddressId());
                }

                rsp = new MsgResponse("Create Order Success", true);
                return Response.status(Response.Status.OK).entity(rsp).build();
            } catch (Exception ex) {
                rsp = new MsgResponse(ex.getMessage(), false);

                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(rsp).build();
            }
        } else {
            rsp = new MsgResponse("Invalid Request Message", false);
            return Response.status(Response.Status.BAD_REQUEST).entity(rsp).build();
        }
    }
    
    

    private CustomerControllerLocal lookupCustomerControllerLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (CustomerControllerLocal) c.lookup("java:global/MakanMaker/MakanMaker-ejb/CustomerController!ejb.session.stateless.CustomerControllerLocal");
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }

    private ShoppingCartControllerLocal lookupShoppingCartControllerLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (ShoppingCartControllerLocal) c.lookup("java:global/MakanMaker/MakanMaker-ejb/ShoppingCartController!ejb.session.stateless.ShoppingCartControllerLocal");
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }

    private OrderControllerLocal lookupOrderControllerLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (OrderControllerLocal) c.lookup("java:global/MakanMaker/MakanMaker-ejb/OrderController!ejb.session.stateless.OrderControllerLocal");
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }
    
    
}
