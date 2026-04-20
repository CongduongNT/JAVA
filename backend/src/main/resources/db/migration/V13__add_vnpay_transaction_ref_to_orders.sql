-- V13: Add VNPay transaction reference columns to orders table
ALTER TABLE orders
    ADD COLUMN vnpay_txn_ref     VARCHAR(32)  NULL COMMENT 'Mã giao dịch gửi sang VNPay (unique per payment attempt)',
    ADD COLUMN vnpay_transaction_no VARCHAR(20) NULL COMMENT 'Mã giao dịch phía VNPay trả về',
    ADD COLUMN vnpay_bank_code   VARCHAR(20)  NULL COMMENT 'Mã ngân hàng thanh toán';

CREATE UNIQUE INDEX idx_orders_vnpay_txn_ref ON orders (vnpay_txn_ref);
