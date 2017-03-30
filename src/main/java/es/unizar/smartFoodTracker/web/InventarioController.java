package es.unizar.smartFoodTracker.web;

import es.unizar.smartFoodTracker.model.Inventario;
import es.unizar.smartFoodTracker.model.Producto;
import es.unizar.smartFoodTracker.model.ProductoInventario;
import es.unizar.smartFoodTracker.model.Usuario;
import es.unizar.smartFoodTracker.repository.InventarioRepository;
import es.unizar.smartFoodTracker.repository.ProductoRepository;
import es.unizar.smartFoodTracker.repository.UsuarioRepository;
import es.unizar.smartFoodTracker.service.InventarioService;
import es.unizar.smartFoodTracker.service.ProductoService;
import es.unizar.smartFoodTracker.service.UsuarioService;
import es.unizar.smartFoodTracker.service.mailService.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by carlos on 26/03/17.
 */
@Controller
@RequestMapping(value = "/api")
public class InventarioController {
    @Autowired
    private EmailService emailService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private ProductoService productoService;

    private final Logger log = Logger.getLogger(UsuarioController.class.getName());

    @GetMapping(value = "/inventario")
    public @ResponseBody List<Inventario> getInventario () {
        return inventarioService.findAll();
    }

    @GetMapping(value = "/inventario/{username:.*}")
    public @ResponseBody List<Inventario> getProductos (@PathVariable String username) {
        Usuario usuario = usuarioService.findByUsername(username);
        if (usuario == null) {
            log.info("Usuario + " + username + " no encontrado");
        }
        return inventarioService.findByUsuario(usuario);
    }

    @PostMapping(value = "/inventario")
    public ResponseEntity<?> postProducto (@RequestBody ProductoInventario productoInventario) throws ParseException {
        Usuario usuario = usuarioService.findByUsername(productoInventario.getNombreUsuario());
        Producto producto = productoService.findByNombre(productoInventario.getNombreProducto());
        if (usuario == null) {
            log.info("Usuario " + usuario.getUsername() + " no se ha encontrado");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        else{
            if (producto == null) {
                log.info("El producto " + producto.getNombre() + " no se ha encontrado");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            else{
                Inventario i = inventarioService.findByUsuarioProducto(usuario, producto);
                if (i == null) {
                    DateFormat formatter = new SimpleDateFormat("dd/MM/yy");
                    Date date = formatter.parse(productoInventario.getFechaCaducidad());
                    Inventario inventario = new Inventario(usuario, producto, productoInventario.getCantidadMinima(),
                            productoInventario.getCantidad(), date);
                    inventarioService.save(inventario);
                    log.info("Nuevo producto " + producto.getNombre() + " añadido al inventario de " + usuario.getUsername());
                    return new ResponseEntity<>(HttpStatus.CREATED);
                }
                else{
                    log.info("El producto " + producto.getNombre() + " ya se encuentra en su inventario");
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }

            }
        }
    }

    @PutMapping(value = "/inventario")
    public ResponseEntity<?> putProducto (@RequestBody ProductoInventario productoInventario) throws ParseException {
        Usuario usuario = usuarioService.findByUsername(productoInventario.getNombreUsuario());
        Producto producto = productoService.findByNombre(productoInventario.getNombreProducto());
        if (usuario == null) {
            log.info("Usuario " + usuario.getUsername() + " no se ha encontrado");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        else{
            if (producto == null) {
                log.info("El producto " + producto.getNombre() + " no se ha encontrado");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            else{
                Inventario i = inventarioService.findByUsuarioProducto(usuario, producto);
                if (i != null) {
                    DateFormat formatter = new SimpleDateFormat("dd/MM/yy");
                    Date date = formatter.parse(productoInventario.getFechaCaducidad());
                    i.setCantidad(productoInventario.getCantidad());
                    i.setCantidadMinima(productoInventario.getCantidadMinima());
                    i.setFechaCaducidad(date);
                    inventarioService.save(i);
//                    if (i.getCantidad() < i.getCantidadMinima()) {
//                        emailService.faltaCantidad(i); // Envia mensaje
//                    }
                    log.info("Producto " + producto.getNombre() + " actualizado en el inventario de " + usuario.getUsername());
                    return new ResponseEntity<>(HttpStatus.OK);
                }
                else{
                    log.info("El producto " + producto.getNombre() + " no está en el inventario de " + usuario.getUsername());
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }

            }
        }
    }

    @DeleteMapping(value = "/inventario")
    public ResponseEntity<?> deleteProducto (@RequestBody ProductoInventario productoInventario){
        Usuario usuario = usuarioService.findByUsername(productoInventario.getNombreUsuario());
        Producto producto = productoService.findByNombre(productoInventario.getNombreProducto());
        if (usuario == null) {
            log.info("Usuario " + usuario.getUsername() + " no se ha encontrado");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        else{
            if (producto == null) {
                log.info("El producto " + producto.getNombre() + " no se ha encontrado");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Inventario i = inventarioService.findByUsuarioProducto(usuario, producto);
            if (i != null) {        //debe existir
                inventarioService.delete(i);
                return new ResponseEntity<>(HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
}