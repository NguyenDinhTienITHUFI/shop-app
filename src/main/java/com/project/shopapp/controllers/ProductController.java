package com.project.shopapp.controllers;

import com.github.javafaker.Faker;
import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.dtos.ProductDTO;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.responses.ProductListResponse;
import com.project.shopapp.responses.ProductResponse;
import com.project.shopapp.services.IProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final IProductService productService;
    @GetMapping("")
    public ResponseEntity<ProductListResponse> getAll(
            @RequestParam("page")     int page,
            @RequestParam("limit")    int limit
    ) {
        PageRequest pageRequest=PageRequest.of(page,limit, Sort.by("createdAt").descending());
        Page<ProductResponse> productPage=productService.getAllProducts(pageRequest);
        int totalPages=productPage.getTotalPages();
        List<ProductResponse>products=productPage.getContent();
        return ResponseEntity.ok(ProductListResponse.builder()
                        .products(products)
                        .totalPages(totalPages)
                .build());
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            Product exProduct=productService.getProductById(id);
            return ResponseEntity.ok(ProductResponse.fromProduct(exProduct));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
    @PostMapping("")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTO productDTO,
//                                           @ModelAttribute("file") List<MultipartFile> files,
                                           BindingResult result) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }
           Product newProduct= productService.createProduct(productDTO);


            return ResponseEntity.ok(newProduct);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());}


    }
    @PostMapping(value = "uploads/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImages(
            @PathVariable("id") Long productId,
            @ModelAttribute("files") List<MultipartFile> files
    ){
        try {
            Product existingProduct = productService.getProductById(productId);
            if(files.size()>ProductImage.MAXIMUM_IMAGES_PER_PRODUCT){
                return ResponseEntity.badRequest().body("You can only upload maximum 5 images");
            }
            files = files == null ? new ArrayList<MultipartFile>() : files;
            List<ProductImage> productImages = new ArrayList<>();
            for (MultipartFile file : files) {
                if(file.getSize() == 0) {
                    continue;
                }
                if(file.getSize() > 10 * 1024 * 1024) {
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                            .body("File is too large! Maximum size is 10MB");
                }
                String contentType = file.getContentType();
                if(contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                            .body("File must be an image");
                }
                String filename = storeFile(file);

                ProductImage productImage = productService.createProductImage(
                        existingProduct.getId(),
                        ProductImageDTO.builder()
                                .imageUrl(filename)
                                .build()
                );
                productImages.add(productImage);
            }
            return ResponseEntity.ok().body(productImages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    private boolean isImageFile(MultipartFile file){
        String contentType=file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
    private String storeFile(MultipartFile file) throws IOException{
        if(!isImageFile(file)||file.getOriginalFilename()== null){
            throw new IOException("Invalid image format");
        }
        String filename=StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
        java.nio.file.Path uploadDir= Paths.get("uploads");
        if(!Files.exists(uploadDir)){
            Files.createDirectories(uploadDir);
        }
        java.nio.file.Path destination=Paths.get(uploadDir.toString(),uniqueFilename);
        Files.copy(file.getInputStream(),destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
    }
    @PutMapping("/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable Long id) {
        return ResponseEntity.ok("updateProduct with id = "+id);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(String.format("Product with id= %d delete successfully",id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
//    @PostMapping("/generateFakeProducts")
    private ResponseEntity<String> generateFakeProducts() {
        Faker faker=new Faker();
        for (int i = 0; i < 1_000; i++) {
            String productName=faker.commerce().productName();
            if(productService.existsByName(productName)){
                continue;
            }
            ProductDTO productDTO=ProductDTO.builder()
                    .name(productName)
                    .price((float)faker.number().numberBetween(10,90_000_000))
                    .description(faker.lorem().sentence())
                    .thumbnail("")
                    .categoryId((long) faker.number().numberBetween(3,5))
                    .build();

            try {
                productService.createProduct(productDTO);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        return ResponseEntity.ok("Fake Product created successfully");
    }
}
