
package controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import data.ProdukRepository;
import model.Produk;

public class KatalogController {

    @FXML
    private TextField namaField;

    @FXML
    private TextField hargaField;

    @FXML
    private ComboBox<String> fotoBox;

    @FXML
    private Label pesanLabel;

    @FXML
    private Label jumlahLabel;

    @FXML
    private TilePane katalogPane;

    private final ProdukRepository repository = new ProdukRepository();

    // Menyimpan ID produk yang sedang dipilih
    private long selectedProductId = -1;

    @FXML
    private void initialize() {
        fotoBox.getItems().add("roti.png");
        fotoBox.getItems().add("susu.png");
        fotoBox.getItems().add("kopi.png");

        fotoBox.setValue("roti.png");

        try {
            showData();
        } catch (SQLException e) {
            pesanLabel.setText("Gagal membaca database. Lihat Output.");
            e.printStackTrace();
        }
    }

    private void showData() throws SQLException {
        ArrayList<Produk> daftar = repository.getAll();

        katalogPane.getChildren().clear();

        for (Produk produk : daftar) {
            VBox kartu = createProductCard(produk);
            katalogPane.getChildren().add(kartu);
        }

        jumlahLabel.setText("Jumlah produk: " + daftar.size());
    }

    private VBox createProductCard(Produk produk) {
        ImageView fotoView = new ImageView();

        fotoView.setFitWidth(170);
        fotoView.setFitHeight(110);
        fotoView.setPreserveRatio(true);

        StackPane bingkai = new StackPane();

        bingkai.setPrefSize(180, 120);
        bingkai.setMinSize(180, 120);
        bingkai.setMaxSize(180, 120);

        URL lokasi = getClass().getResource("/image/" + produk.getFoto());

        if (lokasi != null) {
            Image gambar = new Image(
                    lokasi.toExternalForm(), 170, 110, true, true);

            if (!gambar.isError()) {
                fotoView.setImage(gambar);
                bingkai.getChildren().add(fotoView);
            } else {
                bingkai.getChildren().add(new Label("Gambar tidak terbaca"));
            }
        } else {
            bingkai.getChildren().add(new Label("Gambar tidak tersedia"));
        }

        Label namaLabel = new Label(produk.getNama());
        namaLabel.setWrapText(true);
        namaLabel.getStyleClass().add("nama-produk");

        Label hargaLabel = new Label("Rp" + produk.getHarga());

        Label idLabel = new Label("ID: " + produk.getId());
        idLabel.getStyleClass().add("muted");

        VBox kartu = new VBox();

        kartu.setSpacing(8);
        kartu.setMinWidth(220);
        kartu.setPrefWidth(220);
        kartu.setMaxWidth(220);

        kartu.getStyleClass().add("kartu-produk");

        kartu.getChildren().add(bingkai);
        kartu.getChildren().add(namaLabel);
        kartu.getChildren().add(hargaLabel);
        kartu.getChildren().add(idLabel);

        kartu.setOnMouseClicked(event -> {
            selectProduct(produk.getId());
        });

        kartu.setStyle("-fx-cursor: hand;");

        return kartu;
    }

    @FXML
    private void selectProduct(long id) {
        try {
            Produk produk = repository.getProduk(id);

            if (produk == null) {
                pesanLabel.setText("Produk tidak ditemukan.");
                return;
            }

            selectedProductId = produk.getId();

            namaField.setText(produk.getNama());
            hargaField.setText(String.valueOf(produk.getHarga()));
            fotoBox.setValue(produk.getFoto());

            pesanLabel.setText("Produk berhasil dipilih. ID: " + id);

        } catch (SQLException e) {
            pesanLabel.setText("Gagal mengambil data produk.");
            e.printStackTrace();
        }
    }

    @FXML
    private void saveProduct() {
        try {
            String nama = namaField.getText();
            long harga = Long.parseLong(hargaField.getText().trim());
            String foto = fotoBox.getValue();

            Produk produk = new Produk(nama, harga, foto);

            repository.add(produk);

            resetForm();
            showData();

            pesanLabel.setText("Produk berhasil disimpan.");

        } catch (NumberFormatException e) {
            pesanLabel.setText("Harga harus angka bulat, contoh 12000.");

        } catch (IllegalArgumentException e) {
            pesanLabel.setText(e.getMessage());

        } catch (SQLException e) {
            pesanLabel.setText("Gagal menyimpan. Lihat Output.");
            e.printStackTrace();
        }
    }

    @FXML
    private void updateProduct() {
        if (selectedProductId == -1) {
            pesanLabel.setText("Pilih produk yang ingin diubah.");
            return;
        }

        try {
            Produk produk = repository.getProduk(selectedProductId);

            if (produk == null) {
                pesanLabel.setText("Produk tidak ditemukan.");
                resetForm();
                return;
            }

            produk.setNama(namaField.getText());

            long harga = Long.parseLong(hargaField.getText().trim());
            produk.setHarga(harga);

            produk.setFoto(fotoBox.getValue());

            boolean berhasil = repository.update(produk);

            if (berhasil) {
                resetForm();
                showData();
                pesanLabel.setText("Produk berhasil diperbarui.");
            } else {
                pesanLabel.setText("Produk gagal diperbarui.");
            }

        } catch (NumberFormatException e) {
            pesanLabel.setText("Harga harus berupa angka.");

        } catch (IllegalArgumentException e) {
            pesanLabel.setText(e.getMessage());

        } catch (SQLException e) {
            pesanLabel.setText("Gagal memperbarui produk.");
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteProduct() {
        if (selectedProductId == -1) {
            pesanLabel.setText("Pilih produk yang ingin dihapus.");
            return;
        }

        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);

        konfirmasi.setTitle("Konfirmasi Hapus");
        konfirmasi.setHeaderText("Hapus produk?");
        konfirmasi.setContentText(
                "Produk ID " + selectedProductId + " akan dihapus.");

        if (konfirmasi.showAndWait().orElse(ButtonType.CANCEL)
                == ButtonType.OK) {

            try {
                boolean berhasil = repository.delete(selectedProductId);

                if (berhasil) {
                    resetForm();
                    showData();

                    pesanLabel.setText("Produk berhasil dihapus.");
                } else {
                    pesanLabel.setText("Produk tidak ditemukan.");
                }

            } catch (SQLException e) {
                pesanLabel.setText("Gagal menghapus produk.");
                e.printStackTrace();
            }
        }
    }

    private void resetForm() {
        selectedProductId = -1;

        namaField.clear();
        hargaField.clear();

        fotoBox.setValue("roti.png");
    }
}
