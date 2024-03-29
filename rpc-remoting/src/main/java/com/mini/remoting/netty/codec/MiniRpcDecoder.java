package com.mini.remoting.netty.codec;

import com.mini.model.MiniRpcRequest;
import com.mini.model.MiniRpcResponse;
import com.mini.protocol.MsgHeader;
import com.mini.protocol.MsgProtocol;
import com.mini.protocol.MsgType;
import com.mini.protocol.ProtocolConstant;
import com.mini.serialization.RpcSerialization;
import com.mini.serialization.SerializationFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author carl-xiao
 * @description 解密
 **/
public class MiniRpcDecoder extends ByteToMessageDecoder {

    /**
     * 只有当 ByteBuf 中内容大于协议头 Header 的固定的 18 字节时，才开始读取数据。
     * 即使已经可以完整读取出协议头 Header，但是协议体 Body 有可能还未就绪。所以在刚开始读取数据时，需要使用 markReaderIndex() 方法标记读指针位置，
     * 当 ByteBuf 中可读字节长度小于协议体 Body 的长度时，再使用 resetReaderIndex() 还原读指针位置，说明现在 ByteBuf 中可读字节还不够一个完整的数据包。
     * 根据不同的报文类型 MsgType，需要反序列化出不同的协议体对象。
     * 在 RPC 请求调用的场景下，服务提供者需要将协议体内容反序列化成 MiniRpcRequest 对象；
     * 在 RPC 结果响应的场景下，服务消费者需要将协议体内容反序列化成 MiniRpcResponse 对象。
     *
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < ProtocolConstant.HEADER_TOTAL_LEN) {
            return;
        }

        //标记请求头
        in.markReaderIndex();
        short magic = in.readShort();
        if (magic != ProtocolConstant.MAGIC) {
            throw new IllegalArgumentException("magic number is illegal, " + magic);
        }
        byte version = in.readByte();

        byte serializeType = in.readByte();

        byte msgType = in.readByte();

        byte status = in.readByte();

        long requestId = in.readLong();

        int dataLength = in.readInt();

        //可读内容的字节数小于长度,意外着当前内容不完整,需要重制请求
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }

        //读取body部分
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        MsgType msgTypeEnum = MsgType.findByType(msgType);
        if (msgTypeEnum == null) {
            return;
        }
        MsgHeader header = new MsgHeader();
        header.setMagic(magic);
        header.setVersion(version);
        header.setSerialization(serializeType);
        header.setStatus(status);
        header.setRequestId(requestId);
        header.setMsgType(msgType);
        header.setMsgLength(dataLength);

        RpcSerialization rpcSerialization = SerializationFactory.getRpcSerialization(serializeType);

        switch (msgTypeEnum) {
            case REQUEST:
                MiniRpcRequest request = rpcSerialization.deserialize(data, MiniRpcRequest.class);
                if (request != null) {
                    MsgProtocol<MiniRpcRequest> protocol = new MsgProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(request);
                    out.add(protocol);
                }
                break;
            case RESPONSE:
                MiniRpcResponse response = rpcSerialization.deserialize(data, MiniRpcResponse.class);
                if (response != null) {
                    MsgProtocol<MiniRpcResponse> protocol = new MsgProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(response);
                    out.add(protocol);
                }
                break;
            case HEARTBEAT:
                // TODO
                break;
            default:
                throw new IllegalArgumentException("类型异常");
        }
    }
}
